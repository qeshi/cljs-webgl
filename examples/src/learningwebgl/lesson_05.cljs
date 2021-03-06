(ns learningwebgl.lesson-05
  (:require
    [WebGLUtils]
    [mat4]
    [learningwebgl.common :refer [init-gl init-shaders get-perspective-matrix
                                  get-position-matrix deg->rad animate load-texture]]
    [cljs-webgl.buffers :refer [create-buffer clear-color-buffer clear-depth-buffer draw!]]
    [cljs-webgl.shaders :refer [get-attrib-location]]
    [cljs-webgl.constants.buffer-object :as buffer-object]
    [cljs-webgl.constants.capability :as capability]
    [cljs-webgl.constants.draw-mode :as draw-mode]
    [cljs-webgl.constants.data-type :as data-type]
    [cljs-webgl.typed-arrays :as ta]))


(defn ^:export start []
  (let [canvas      (.getElementById js/document "canvas")
        gl          (init-gl canvas)
        shader-prog (init-shaders gl)
        cube-vertex-position-buffer
                    (create-buffer gl
                      (ta/float32 [
                                  ; Front face
                                  -1.0, -1.0,  1.0,
                                   1.0, -1.0,  1.0,
                                   1.0,  1.0,  1.0,
                                  -1.0,  1.0,  1.0,

                                  ; Back face
                                  -1.0, -1.0, -1.0,
                                  -1.0,  1.0, -1.0,
                                   1.0,  1.0, -1.0,
                                   1.0, -1.0, -1.0,

                                  ; Top face
                                  -1.0,  1.0, -1.0,
                                  -1.0,  1.0,  1.0,
                                   1.0,  1.0,  1.0,
                                   1.0,  1.0, -1.0,

                                  ; Bottom face
                                  -1.0, -1.0, -1.0,
                                   1.0, -1.0, -1.0,
                                   1.0, -1.0,  1.0,
                                  -1.0, -1.0,  1.0,

                                  ; Right face
                                   1.0, -1.0, -1.0,
                                   1.0,  1.0, -1.0,
                                   1.0,  1.0,  1.0,
                                   1.0, -1.0,  1.0,

                                  ; Left face
                                  -1.0, -1.0, -1.0,
                                  -1.0, -1.0,  1.0,
                                  -1.0,  1.0,  1.0,
                                  -1.0,  1.0, -1.0])
                      buffer-object/array-buffer
                      buffer-object/static-draw
                      3)

        cube-vertex-texture-coords-buffer
                    (create-buffer gl
                      (ta/float32 [
                                  ; Front face
                                  0.0, 0.0,
                                  1.0, 0.0,
                                  1.0, 1.0,
                                  0.0, 1.0,

                                  ; Back face
                                  1.0, 0.0,
                                  1.0, 1.0,
                                  0.0, 1.0,
                                  0.0, 0.0,

                                  ; Top face
                                  0.0, 1.0,
                                  0.0, 0.0,
                                  1.0, 0.0,
                                  1.0, 1.0,

                                  ; Bottom face
                                  1.0, 1.0,
                                  0.0, 1.0,
                                  0.0, 0.0,
                                  1.0, 0.0,

                                  ; Right face
                                  1.0, 0.0,
                                  1.0, 1.0,
                                  0.0, 1.0,
                                  0.0, 0.0,

                                  ; Left face
                                  0.0, 0.0,
                                  1.0, 0.0,
                                  1.0, 1.0,
                                  0.0, 1.0, ])
                      buffer-object/array-buffer
                      buffer-object/static-draw
                      2)

        cube-vertex-indices
                    (create-buffer gl
                      (ta/unsigned-int16 [
                                  0, 1, 2,      0, 2, 3,      ; Front face
                                  4, 5, 6,      4, 6, 7,      ; Back face
                                  8, 9, 10,     8, 10, 11,    ; Top face
                                  12, 13, 14,   12, 14, 15,   ; Bottom face
                                  16, 17, 18,   16, 18, 19,   ; Right face
                                  20, 21, 22,   20, 22, 23])  ; Left face
                      buffer-object/element-array-buffer
                      buffer-object/static-draw
                      1)

        cube-matrix (get-position-matrix [ 0.0 0.0 -5.0])

        vertex-position-attribute (get-attrib-location gl shader-prog "aVertexPosition")
        texture-coord-attribute   (get-attrib-location gl shader-prog "aTextureCoord")
        perspective-matrix (get-perspective-matrix gl)
        two-degrees (deg->rad 2)]
    (load-texture gl "nehe.gif" (fn [nehe-texture]
      (animate
        (fn [frame] ; frame is not used

          (clear-color-buffer gl 0.0 0.0 0.0 1.0)
          (clear-depth-buffer gl 1)

          ; Doesn't replicate the same tumbling that the original does.
          (mat4/rotate
            cube-matrix
            cube-matrix
            two-degrees
            (ta/float32 [1 1 1]))

          (draw!
            gl
            :shader shader-prog
            :draw-mode draw-mode/triangles
            :count (.-numItems cube-vertex-indices)
            :attributes [{:buffer cube-vertex-position-buffer :location vertex-position-attribute}
                         {:buffer cube-vertex-texture-coords-buffer :location texture-coord-attribute}]
            :uniforms [{:name "uPMatrix" :type :mat4 :values perspective-matrix}
                       {:name "uMVMatrix" :type :mat4 :values cube-matrix}]
            :textures [{:name "uSampler" :texture nehe-texture}]
            :element-array {:buffer cube-vertex-indices :type data-type/unsigned-short :offset 0}
            :capabilities {capability/depth-test true})))))))
