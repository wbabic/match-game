(ns async-play.color)

;; color
(defprotocol color
  (css-string [color]))

(defrecord hsla [h s l a]
  color
  (css-string [c]
    (str "hsla(" (:h c) ", "
         (:s c) "%, "
         (:l c) "%, "
         a
         ")")))

(defrecord rgba
    [r g b a]
  color
  (css-string [c]
    (str "rgba("
         (clojure.string/join "," (vals c))
         ")")))

(comment
  (hsla. 120 100 50 0.5)
  (css-string (hsla. 120 100 50 0.5))
  (css-string (rgba. 255 0 0 1))
  ()
  )
