(ns laranjas.code-editor)

(defn set-div-content [v output-id]
  (-> (js/document.getElementById output-id)
      (.-innerHTML)
      (set! v)))

(defn try-eval [v]
  (try (js/scittle.core.eval_string v)
    (catch js/Error e
      (str "ERROR: " e))))

(defn read-eval-input [input-id]
  (-> input-id
      js/document.getElementById 
      .-value
      try-eval))

(defn fetch-file-content []
  (-> (.fetch js/window "/src/laranjas/main.cljs")
      (.then #(.text %))
      (.then #(set-div-content % "code-area"))
      (.then #(read-eval-input "code-area"))))

(fetch-file-content)

(set! (.-read_input js/window)
      #(set-div-content (read-eval-input "code-area")
                        "output-area"))

(read-eval-input "code-area")
