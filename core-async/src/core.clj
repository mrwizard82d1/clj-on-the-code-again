(ns core
  (:require [clojure.core.async :as a :refer [chan >!! <!! thread
                                              put! take!]]))

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
