(ns async-play.web.match
  (:require [cljs.core.async :as async :refer [chan <! >! put close]]
            [async-play.web.draw :as draw]
            [async-play.web.async-utils :as utils])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

(defn target-stream [target-sequence]
  (let [in (chan)]
    (go
     (loop [data target-sequence]
       (let [d (first data)]
         (if d
           (do
             (>! in d)
             (recur (rest data)))))))
    in))

(def test-data [:new :sigma :sigma :rho :match])

(defn log [s]
  (.log js/console s))

(def square
  {:vertices [:v1 :v2 :v3 :v4]
   :center :v0
   :position-map {:p0 {:x 250 :y 250}
                  :p1 {:x 350 :y 250}
                  :p2 {:x 250 :y 150}
                  :p3 {:x 150 :y 250}
                  :p4 {:x 250 :y 350}}
   :colors {:v0 "hsla(60,100%,50%,1.0)"
            :v1 "hsla(90,100%,50%,0.5)"
            :v2 "hsla(180,100%,50%,0.5)"
            :v3 "hsla(270,100%,50%,0.5)"
            :v4 "hsla(360,100%,50%,0.5)"}
   :vertices-map {:v0 :p0
                  :v1 :p1
                  :v2 :p2
                  :v3 :p3
                  :v4 :p4}})

(defn position [vertex polygon]
  (let [position (-> polygon :vertices-map vertex)]
    (-> polygon :position-map position)))

(defn color [vertex polygon]
  (-> polygon :colors vertex))

(defn vertex-at-position [position vertices-map]
  (ffirst (filter (fn [[k v]] (= v position)) vertices-map)))

;; the permutations are sigma and pho
(defn sigma
  "swap positions of vertices at postions p2 and p4"
  [vertices-map]
  (let [v2 (vertex-at-position :p2 vertices-map)
        v4 (vertex-at-position :p4 vertices-map)]
    (assoc vertices-map v2 :p4 v4 :p2)))

(defn rho
  "cycle positions of v1 v2 v3 v4"
  [vertices-map]
  (let [v1 (vertex-at-position :p1 vertices-map)
        v2 (vertex-at-position :p2 vertices-map)
        v3 (vertex-at-position :p3 vertices-map)
        v4 (vertex-at-position :p4 vertices-map)]
    (assoc vertices-map v1 :p2 v2 :p3 v3 :p4 v4 :p1)))

(def target-sequence [(update-in square [:vertices-map] (comp sigma rho))
                      (update-in square [:vertices-map] (comp rho sigma))
                      (update-in square [:vertices-map] (comp rho rho sigma))])

(defn main-app [start-state target-stream in]
  (go
   (loop [state start-state]
     (draw/draw (:source state) "tri-canvas")
     (let [d (<! in)]
       (log (name d))
       (recur
        (condp = d
          :new (do
                 (let [t (<! target-stream)]
                   (draw/draw t "tri-canvas-source")
                   (assoc state :target t)))
          :sigma (update-in state [:source :vertices-map] sigma)
          :rho (update-in state [:source :vertices-map] rho)
          :match (if (= (-> state :target :vertices-map)
                        (-> state :source :vertices-map))
                   (do
                     (log "we have a match")
                     ;; set next source
                     ;; init target
                     (let [next-target (<! target-stream)]
                       (draw/draw next-target "tri-canvas-source")
                       (assoc state
                         :source (:source start-state)
                         :target next-target)))
                   (do
                     (log "sorry, no match")
                     state))))))))

(defn app-loop [start-state target-stream in]
  (main-app start-state target-stream in))

(def repl-in (chan))

(defn inputs []
  (utils/merge-chans
   repl-in
   (utils/filter-chan #{:rho :sigma :match :new} (utils/key-input-channel))
   ))

(def target-in (target-stream (cycle target-sequence)))

(app-loop {:source square} target-in (inputs))

(comment
  (go (>! repl-in :new))
  (go (>! repl-in :rho))
  (go (>! repl-in :sigma))
  (go (>! repl-in :match))
  )
