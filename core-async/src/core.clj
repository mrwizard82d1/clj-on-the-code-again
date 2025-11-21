(ns core
  (:require [clojure.core.async :as a :refer [chan >!! <!! thread
                                              put! take! go >! <!]]
            [hato.client :as hc]
            [clojure.data.json :as json]))

;; Be sure to evaluate all these forms in the `core` namespace

;; Create a channel
(chan)

;; Note running `doseq` outside a thread prints all the values bound
;; to `x`
(doseq [x (range 1 5)]
      (println x))

;; Push values onto a channel from one thread and pull values off
;; on another.
(let [c (chan 2)]
  (thread
    (doseq [x (range 1 5)]
      ;; The producer want to put four items on the queue; however, the
      ;; size of the queue is **two** so it blocks any and all producters
      ;; once it is full.
      (>!! c x)
      (println "Put value " x " on channel")))
  (thread
    ;; Introducing a call to `Thread/sleep` avoids intermingling of
    ;; producer and consumer output. Consequently, the result more
    ;; clearly demonstrates the producer waiting for the consumer to
    ;; remove items.
    (Thread/sleep 1000)
    (doseq [x (range 1 3)]
      ;; This consumer takes **two** items off the queue and then stops.
      (println "from chan" (<!! c)))))

;; Demonstrate `put!` on a channel and `take!` from a channel

(let [c (chan)]
  (thread
    (put! c
          "On the code again"
          (fn [sent?]
            ;; Optional callback that indicates a value was sent.
            (println "has been sent?" sent?))))
  (thread
    (take! c (fn [value]
               (println (str "taken='" value "'"))))))

;; This code works; however, creating `thread`s is relatively expensive.
;; Creating `go` routines is **much cheaper** because a `go` routine
;; **does not** create a **system thread**.

;; Push values onto a channel and pull values off using `go`.
;;
;; Note that the output is again **mixed up**. On my M1 Mac, I see
;;
;; > from chan 1
;; > Put value 1 on channel
;; > Put value 2 on channel
;; > from chan 2
;;
(let [c (chan)]
  (go
    (doseq [x (range 1 5)]
      ;; The producer want to put four items on the queue; however, the
      ;; size of the queue is **two** so it blocks any and all producters
      ;; once it is full.
      (>! c x)
      (println "Put value " x " on channel")))
  (go
    ;; Introducing a call to `Thread/sleep` avoids intermingling of
    ;; producer and consumer output. Consequently, the result more
    ;; clearly demonstrates the producer waiting for the consumer to
    ;; remove items.
    (Thread/sleep 1000)
    (doseq [x (range 1 3)]
      ;; This consumer takes **two** items off the queue and then stops.
      (println "from chan" (<! c)))))

;; Get a "test / phony" user
(defn fetch-user [user-id]
  (-> (str "https://reqres.in/api/users/" user-id)
      hc/get
      :body
      json/read-json
      :data))

(fetch-user 3)

;; (Fake) Email a user
(defn email-user [email]
  ;; Simulate reading from the network
  (Thread/sleep 1000)
  (println "Email sent to" email))

(email-user "test@test.com")

;; This function does not quite work as in the video. I suspect something
;; has changed with the reqres API. I've noticed that it no longer
;; recognizes a user with an id of 1. And, attempting to fetch a user
;; with an id of `3` also generates an exception because of a 401
;; status code.
(defn process-users []
  (let [c (chan)]
    (thread
      (doseq [x (range 2 3)]
        (>!! c (fetch-user x))))
    (thread
      (loop []
        (when-some [user (<!! c)]
          (email-user (:email user)))
        (recur)))))

(process-users)

(defn process-users-go []
  (let [c (chan)]
    (go
      (doseq [x (range 2 3)]
        (>! c (fetch-user x))))
    (go
      (loop []
        (when-some [user (<! c)]
          (email-user (:email user)))
        (recur)))))

(process-users-go)
