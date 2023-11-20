(ns laranjas.main
  (:require [goog.object :as g]))

(def game-state (atom {}))

(def c (js/document.getElementById "app"))

(def gl (.getContext c "webgl"))

(def dimensions
  (atom {:x 100 :y 100}))

(defn reset-dimensions []
  (let [width js/window.innerWidth
        height js/window.innerHeight]
    (g/set c "width" width)
    (g/set c "height" height)
    (reset! dimensions {:x width
                        :y height})
    (.viewport gl 0 0 width height)))

(defn clear []
  (.clearColor gl 0 0 0 1)
  (.clear gl (g/get gl "COLOR_BUFFER_BIT")))

(defn create-shader [shader-type source]
  (let [shader (.createShader gl shader-type)]
    (.shaderSource gl shader source)
    (.compileShader gl shader)
    (js/console.log
     (str "compile shader: "
          (.getShaderParameter
           gl shader (g/get gl "COMPILE_STATUS"))))
    shader))

(def frag-shader-source
  "
precision mediump float;

void main() {
  gl_FragColor = vec4(1, 0, 0.5, 1);
}
")

(def vert-shader-source
  "
attribute vec4 a_position; 

void main() {
  gl_Position = a_position;
}
")

(defn frag-shader []
  (create-shader (g/get gl "FRAGMENT_SHADER")
                 frag-shader-source))

(defn vert-shader []
  (create-shader (g/get gl "VERTEX_SHADER")
                 vert-shader-source))

(defn create-prog [vert frag]
  (let [program (.createProgram gl)]
    (.attachShader gl program vert)
    (.attachShader gl program frag)
    (.linkProgram gl program)
    (js/console.log
     (str "link status: "
          (.getProgramParameter gl program
                                (g/get gl "LINK_STATUS"))))
    program))

(defn clean-up []
  (.deleteProgram gl (:prog @game-state))
  (.deleteShader  gl (:vs @game-state))
  (.deleteShader  gl (:fs @game-state)))

(defn -main []
  (.addEventListener js/window
                     "resize"
                     reset-dimensions)
  (let [vs (vert-shader)
        fs (frag-shader)
        prog (create-prog vs fs)
        _ (reset! game-state {:vs vs :fs fs :prog prog})
        pos-attr-loc (.getAttribLocation gl
                                         prog
                                         "a_position")
        pos-buff (.createBuffer gl)
        _ (.bindBuffer gl
                       (g/get gl "ARRAY_BUFFER")
                       pos-buff)
        points [0 0 0 0.5 0.7 0]
        _ (.bufferData gl
                       (g/get gl "ARRAY_BUFFER")
                       (js/Float32Array. points)
                       (g/get gl "STATIC_DRAW"))]
    (reset-dimensions)
    (clear)
    (.useProgram gl prog)
    (.enableVertexAttribArray gl pos-attr-loc)
    (.bindBuffer gl
                 (g/get gl "ARRAY_BUFFER")
                 pos-buff)
    (let [size 2
          dtype (g/get gl "FLOAT")
          normalize? false
          stride 0
          offset 0]
      (.vertexAttribPointer gl
                            pos-attr-loc
                            size
                            dtype
                            normalize?
                            stride
                            offset))
    (let [prim-type (g/get gl "TRIANGLES")
          offset 0
          cnt 3]
      (.drawArrays gl prim-type offset cnt))))

(comment
  (clean-up)

  (-main)

  ,)
