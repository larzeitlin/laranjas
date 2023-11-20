(ns laranjas.main
  (:require [goog.object :as g]))

(defonce game-state (atom {:stop false
                           :p-systems []}))

(def c (js/document.getElementById "app"))

(def ctx (.getContext c "2d"))

(defonce dimensions (atom {:x 100
                           :y 100}))

(defonce mouse (atom {:x 0
                      :y 0}))

(defn dim-frac [dim frac]
  (-> @dimensions dim (* frac)))

(defn abs->frac [dim v]
  (/ v (-> @dimensions dim)))

(def frac-x (partial dim-frac :x))
(def frac-y (partial dim-frac :y))

(defn clear []
  (g/set ctx "fillStyle" "black")
  (.fillRect ctx 0 0 (frac-x 1) (frac-y 1)))

(defn circle [center-x center-y r fill]
  (.beginPath ctx)
  (.arc ctx
        (frac-x center-x)
        (frac-y center-y)
        r
        0
        (* 2 js/Math.PI))
  (.closePath ctx)
  (g/set ctx "fillStyle" fill)
  (.fill ctx))

(defn rand-from-range [r-min r-max]
  (+ r-min (* (rand) (- r-max r-min))))

(defn make-particle
  [{:keys [life-min life-max x y
           v-max
           r-min r-max]}]
  {:life (rand-from-range life-max life-min)
   :position [x y]
   :v [(rand-from-range (- v-max) v-max)
       (rand-from-range (- v-max) v-max)]
   :r (rand-from-range r-min r-max)
   :color [(+ 200 (rand-int 55))
           (+ 100 (rand-int 100)) 0 (rand)]
   :spin (rand 0.03)})

(defn make-p-system
  [{:keys [n-parts]
    :as args}]
  (let [parts (->> #(make-particle args)
                   repeatedly
                   (take n-parts))]
    (assoc args :ps parts)))

(defn rotate-vector
  [[vx vy] theta]
  [(+ (* vx (js/Math.cos theta))
      (* vy (- (js/Math.sin theta))))
   (+ (* vy (js/Math.cos theta))
      (* vx (js/Math.sin theta)))])

(defn update-particle
  [{:keys [r-decay] :as p-system}
   {:keys [position v life r spin]
    :as p}]
  (let [[px py] position
        [vx vy] (rotate-vector v spin)
        new-vx (if (<= 0 px 1) vx (- vx))
        new-vy (if (<= 0 py 1) vy (- vy))
        new-x (+ px new-vx)
        new-y (+ py new-vy)]

    (if (<= life 0)
      (make-particle p-system)
      (assoc p
             :position
             [new-x new-y]
             :v [new-vx new-vy]
             :life (dec life)
             :r (* r-decay r)))))

(defn update-p-system [p-system]
  (let [update-p-fn (partial update-particle p-system)]
    (-> p-system
        (update :ps #(mapv update-p-fn %))
        (assoc :x (:x @mouse)
               :y (:y @mouse)))))

(defn update-game []
  (swap! game-state update :p-systems
         #(mapv update-p-system %))
  (swap! game-state update :p-systems
         #(mapv update-p-system %)))

(defn draw []
  (clear)
  (doseq [p-sys (:p-systems @game-state)
          {:keys [position r color]} (:ps p-sys)]
    (let [[col-r col-g col-b col-a] color
          [x y] position]
      (circle x y r (str "rgba(" col-r
                         ", "    col-g
                         ", "    col-b
                         ", "    col-a ")")))))

(defn reset-mouse [e]
  (reset! mouse
          {:x (abs->frac
               :x (- (g/get e "clientX")
                     (g/get c "offsetLeft")))
           :y (abs->frac
               :y (- (g/get e "clientY")
                     (g/get c "offsetTop")))}))

(defn reset-dimensions [_]
  (let [width js/window.innerWidth
        height js/window.innerHeight]
    (g/set c "width" width)
    (g/set c "height" height)
    (reset! dimensions {:x width
                        :y height}))
  true)

(defn step [ts]
  (let [interval (- ts (or (@game-state :ts) ts))]
    (swap! game-state
           assoc
           :ts ts
           :interval interval))
  (update-game)
  (draw)
  (when-not (:stop @game-state)
    (js/window.requestAnimationFrame step)))

(defn freeze []
  (swap! game-state update :stop not))

(defn reset-game []
  (reset! game-state {:stop false
                      :p-systems []})
  nil)

(defn -main []
  (freeze)
  (reset-game)
  (reset-dimensions nil)
  (.addEventListener js/window
                     "resize"
                     reset-dimensions)
  (.addEventListener c
                     "mousemove"
                     reset-mouse)
  (swap! game-state
         update
         :p-systems
         conj
         (make-p-system {:x 0.5
                         :y 0.5
                         :r-min 1
                         :r-max 30
                         :n-parts 1000
                         :life-max 500
                         :life-min 100
                         :v-max 0.003
                         :r-decay 0.998}))

  (js/window.requestAnimationFrame step))

(-main)
