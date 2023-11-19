(ns laranjas.main
  (:require [laranjas.hiccup :as h]))

(def app
  (h/html-str [:h2 "howdy worldy"]))

(defn -main []
  (let [c (js/document.getElementById "app")
        ctx (.getContext c "2d")
        width (.-width c)
        height (.-height c)]
    (set! (.-strokeStyle ctx) "black")
    (.moveTo ctx 0 0)
    (.lineTo ctx width height)
    (.stroke ctx)))

(-main)
