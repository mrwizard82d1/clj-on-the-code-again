(ns core
  (:require [clojure.core.async :as a :refer [chan >!! <!! thread]]))

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
    (doseq [x (range 1 3)]
      ;; This consumer takes **two** items off the queue and then stops.
      (println "from chan" (<!! c)))))

;; As a consequence, the output, although intermingled between producer
;; and consumer, demonstrates both producer and consumer working. The
;; producer puts, at most, **two** items on the queue before it waits
;; for the consumer to "free" space by consuming items. Once the
;; consumer has consumed the first two items put onto the queue by
;; the producer, the consumer thread exits because it is finished.
;; Additionally, the consumer is now free to put up to two additional
;; items onto the queue.
;;
;; Notice that, at least on my M1 Mac, I see the ouptut from the
;; consumer and producer intermingled in an unpredictable way.
