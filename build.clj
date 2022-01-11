(ns build
  (:require [clojure.string :as str]
            [clojure.tools.build.api :as b]))

(def lib 'babashka/pod-babashka-etaoin)
(def version (str/trim (slurp "resources/POD_VERSION")))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :ns-compile '[pod.babashka.etaoin]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'pod.babashka.etaoin}))
