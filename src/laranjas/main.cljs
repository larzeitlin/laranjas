(ns laranjas.main
  (:require [goog.object :as g]))

(def c (js/document.getElementById "app"))
(defonce ctx (.getContext c "2d"))

(def dimensions (atom {:x 100
                       :y 100}))

(def mouse (atom {:x 0
                  :y 0}))

(defn dim-frac [dim frac]
  (-> @dimensions dim (* frac)))

(defn abs->frac [dim v]
  (/ v (-> @dimensions dim)))

(def x (partial dim-frac :x))
(def y (partial dim-frac :y))

(defn clear []
  (.call (g/get ctx "clearRect")
         ctx 0 0 (x 1) (y 1)))

(defn line [[start-x start-y]
            [end-x end-y]]
  (.beginPath ctx)
  (.moveTo ctx (x start-x) (y start-y))
  (.lineTo ctx (x end-x) (y end-y))
  (.closePath ctx)
  (.stroke ctx))

(defn draw []
  (let [mouse-coords ((juxt :x :y) @mouse)]
    (clear)
    (line [0 0] mouse-coords)
    (line [0 0.5] mouse-coords)
    (line [0 1] mouse-coords)
    (line [0.5 1] mouse-coords)
    (line [1 0] mouse-coords)
    (line [1 0.5] mouse-coords)
    (line [1 1] mouse-coords)
    (line [0.5 0] mouse-coords)
    ))

(defn reset-mouse [e]
  (reset! mouse
          {:x (abs->frac :x (- (g/get e "clientX")
                               (g/get c "offsetLeft")))
           :y (abs->frac :y (- (g/get e "clientY")
                               (g/get c "offsetTop")))})
  (draw))

(defn reset-dimensions [_]
  (js/console.log "resize!!")
  (let [width js/window.innerWidth
        height js/window.innerHeight]
    (g/set c "width" width)
    (g/set c "height" height)
    (reset! dimensions {:x width
                        :y height}))
  true)

(defn -main []
  (reset-dimensions nil)
  (.addEventListener
   js/window
   "resize" reset-dimensions)  
  (.addEventListener
   c "mousemove" reset-mouse))

(-main)
