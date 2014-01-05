(ns async-play.transforms
  (:require [clojure.core.async :refer :all]))

(defn transform-channel [] (chan))

(defn log-transforms [t-chan]
  (go
   (while true
     (let [transform (<! t-chan)]
       (println transform)))))

(defn apply-transform [t-chan transform]
  (go
   (>! t-chan transform)))


(comment
  (def t-chan (transform-channel))
  (log-transforms t-chan)
  (apply-transform t-chan [:move-to 0 0])
  )
