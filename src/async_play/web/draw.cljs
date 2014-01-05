(ns async-play.web.draw)

(defn position [vertex polygon]
  (let [position (-> polygon :vertices-map vertex)]
    (-> polygon :position-map position)))

(defn color [vertex polygon]
  (-> polygon :colors vertex))

(defn surface [id]
  (let [surface (.getElementById js/document id)]
    [(.getContext surface "2d")
     (. surface -width)
     (. surface -height)]))

(defn draw-vertex [surface vertex poly]
  (let [{:keys [x y]} (position vertex poly)
        color (color vertex poly)]
    (.beginPath surface)
    (.arc surface x y 3 0 (* 2 Math/PI) true)
    (.stroke surface)
    (set! (. surface -fillStyle) color)
    (.fill surface)
    (. surface (closePath))
    ))

(defn draw-vertices [surface polygon]
  (doseq [vertex (:vertices polygon)]
    (draw-vertex surface vertex polygon)))

(defn midpoint [v1 v2]
  {:x (/ (+ (:x v1) (:y v1)) 2)
   :y (/ (+ (:x v2) (:y v2)) 2)})

(defn draw-sub-face [surface center v1 v2 c1 c2]
  (let [v12 (midpoint v1 v2)]
    (.beginPath surface)
    (.moveTo surface (-> center :x) (-> center :y))
    (.lineTo surface (-> v1 :x) (-> v1 :y))
    (.lineTo surface (-> v2 :x) (-> v2 :y))
    (set! (. surface -fillStyle) c1)
    (.fill surface)    
    (. surface (closePath))

    (comment
      (.beginPath surface)
      (.moveTo surface (-> center :x) (-> center :y))
      (.lineTo surface (-> v2 :x) (-> v2 :y))
      (.lineTo surface (-> v12 :x) (-> v12 :y))
      (set! (. surface -fillStyle) c2)
      (.fill surface)    
      (. surface (closePath)))))

(defn draw-sub-faces [surface poly]
  (let [vertices (:vertices poly)
        center (:center poly)
        n (count vertices)
        sub-faces (take n (partition 2 1 (cycle vertices)))]
    (doseq [[vi vj] sub-faces]
      (draw-sub-face surface
                     (position center poly)
                     (position vi poly)
                     (position vj poly)
                     (color vi poly)
                     (color vj poly)))))

(defn ^:export draw
  "draw polygon in context"
  [polygon canvas-id]
  (let [[surface width height] (surface canvas-id)
        x0 (/ width 2)
        y0 (/ height 2)]
    (set! (. surface -fillStyle) "rgb(120,120,120)")
    (.fillRect surface 0 0 width height)
    (set! (. surface -strokeStyle) "rgb(25, 0, 25)")
    (set! (. surface -lineWidth) 1)
    (.beginPath surface)
    (.moveTo surface x0 10)
    (.lineTo surface x0 (- height 10))
    (.moveTo surface 10 y0)
    (.lineTo surface (- width 10) y0)
    (.moveTo surface 10 10)
    (.lineTo surface (- width 10) (- height 10))
    (.moveTo surface 10 (- height 10))
    (.lineTo surface (- width 10) 10)
    (.stroke surface)
    (. surface (closePath))

    (.beginPath surface)
    (set! (. surface -strokeStyle) "rgb(20,20,20)")
    (.arc surface x0 y0 100 0 (* 2 Math/PI) true)
    (.stroke surface)

    (draw-sub-faces surface polygon)
    (draw-vertices surface polygon)
    (draw-vertex surface :v0 polygon)
    :ok))
