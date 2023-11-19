(ns laranjas.main)

(defn -main []
  (let [c (js/document.getElementById "app")
        ctx (.getContext c "2d")
        width (.-width c)
        height (.-height c)]
    (set! (.-strokeStyle ctx) "orange")
    (.moveTo ctx 0 0)
    (.lineTo ctx width height)
    (.stroke ctx)))

(-main)
