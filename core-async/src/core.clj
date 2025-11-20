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
(let [c (chan)]
  ;; Better usage. Use `thread` from `core.async`. This action
  ;; creates a `channel` (with the thread hidden) which then
  ;; allows one to perform asynchronous operations like `put!!`
  (thread
    (doseq [x (range 1 5)]
      ;; Put (with blocking) the value, `x` onto our channel, `c`
      ;; "The first `!` says, 'This is a side-effect.' The second
      ;; `!` says that this is blocking."
      (>!! c x)))
  ;; Better usage. Use `thread` from `core.async`. This action
  ;; creates a `channel` (with the thread hidden) which then
  ;; allows one to perform asynchronous operations like `take!!`
  (thread
    (doseq [x (range 1 5)]
      ;; Take (with blocking) the next value from our channel, `c`
      (println "from chan" (<!! c)))))
