(ns async-play.geometry
  (:require [clojure.core.async :refer :all]
            [async-play.transforms :as transforms]))

(defn initial-situation []
  {:position [0 0]
   :orientation 0
   :size 1.0})

(defn move-to [situation new-position]
  (merge situation {:position new-position}))

(defn move-by [situation relative-position]
  (let [old-position (:position situation)]
    (move-to situation (mapv + old-position relative-position))))

(defn scale-to [situation scale]
  (merge situation {:size scale}))

(defn scale-by [situation scale-factor]
  (let [old-scale (:size situation)]
    (scale-to situation (* scale-factor old-scale))))

(defn orient-to [situation orientation]
  (merge situation {:orientation orientation}))

(defn orient-by [situation orientation-delta]
  (let [old-orientation (:orientation situation)]
    (orient-to situation (+ orientation-delta old-orientation))))

(comment
  (move-to (initial-situation) [10 10])
  (move-by (move-to (initial-situation) [10 10]) [5 4])

  (scale-to (initial-situation) 10)
  (scale-by (scale-to (initial-situation) 10) 0.5)

  (orient-to (initial-situation) 3.14)
  (orient-by (orient-to (initial-situation) 3.14) 1.2)
  
  )

(comment
  ;;transform
  [:move-to [x y]]   ;; move to absolute postion [x y]
  [:move-by [x y]]   ;; move relative to current position [+x +y]
  [:scale-to r]      ;; set to absolute scale
  [:scale-by r]      ;; scale factor * orig scale
  [:orient-to theta] ;; set orientation to given angle in radians
  [:orient-by theta] ;; add given orientation to original
  )

;; now, lets create a stateful triangle that can accept transforms
;; that can change its state

(defn transform-fn
  "return a fn for given transform data structure
the function will take a situation and return a new
transformed situation"
  [transform]
  (let [op (first transform)
        arg (second transform)]
    (case op
      :move-to #(move-to % arg)
      :move-by #(move-by % arg)
      :scale-to #(scale-to % arg)
      :scale-by #(scale-by % arg)
      :orient-to #(orient-to % arg)
      :orient-by #(orient-by % arg))))

(defn initial-style [type]
  {})

(defn receive-inputs
  "subscribe to in channel
apply each transform to given situation atom
send transform to given out channel"
  [transform-channel situation out]
  (go
   (while true
     (let [transform (<! transform-channel)]
       (swap! situation (transform-fn transform))
       (>! out transform)))))

(def abc
  (let [vertices 3
        situation (atom (initial-situation))
        style (initial-style :rgb)
        in (transforms/transform-channel)
        out (chan)]
    (receive-inputs in situation out)
    (transforms/log-transforms out)
    {:name :tri-abc
     :vertices vertices
     :situation situation
     :style style
     :input in
     :output out}))
