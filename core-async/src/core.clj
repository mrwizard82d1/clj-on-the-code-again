(ns core
  (:require [clojure.core.async :as a :refer [chan >!!]]))

;; Create a channel
(chan)

;; Note running `doseq` outside a thread prints all the values bound
;; to `x`
(doseq [x (range 1 5)]
      (println x))

;; Push values onto a channel from one thread and pull values off
;; on another.
(let [c chan]
  (future ;; starts a thread executing the next form
    (doseq [x (range 1 5)]
      ;; Put (with blocking) the value, `x` onto our channel, `c`
      ;; "The first `!` says, 'This is a side-effect.' The second
      ;; `!` says that this is blocking."
      (>!! c x))))
