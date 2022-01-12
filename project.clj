(defproject babashka/pod-babashka-etaoin
  #=(clojure.string/trim
     #=(slurp "resources/POD_VERSION"))
  :description "babashka etaoin pod"
  :url "https://github.com/borkdude/pod-babashka-etaion"
  :scm {:name "git"
        :url "https://github.com/borkdude/pod-babashka-etaoin"}
  :license {:name "Eclipse Public License 1.0"
            :url "http://opensource.org/licenses/eclipse-1.0.php"}
  :source-paths ["src"]
  :resource-paths ["resources"]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [borkdude/etaoin-graal "0.4.6-ae21d07-2"]
                 [nrepl/bencode "1.1.0"]]
  :profiles {:uberjar {:global-vars {*assert* false}
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.spec.skip-macros=true"]
                       :aot [pod.babashka.etaoin]
                       :main pod.babashka.etaoin}}
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username :env/clojars_user
                                    :password :env/clojars_pass
                                    :sign-releases false}]])
