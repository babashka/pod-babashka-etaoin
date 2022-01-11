(ns pod.babashka.etaoin
  {:clj-kondo/config '{:lint-as {pod.babashka.etaoin/def-etaoin clojure.core/def}}}
  (:refer-clojure :exclude [read read-string])
  (:require [bencode.core :as bencode]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [etaoin.api :as eta]
            [etaoin.query])
  (:import [java.io PushbackInputStream])
  (:gen-class))

(def stdin (PushbackInputStream. System/in))

(defn write [v]
  (bencode/write-bencode System/out v)
  (.flush System/out))

(defn read-string [^"[B" v]
  (String. v))

(defn read []
  (bencode/read-bencode stdin))

(def debug? false)

(defn debug [& strs]
  (when debug?
    (binding [*out* (io/writer System/err)]
      (apply println strs))))

(defn next-browser-id []
  (str (java.util.UUID/randomUUID)))

(def browsers (atom {}))

(defn create-driver [& args]
  (let [browser (apply #'eta/-create-driver args)
        browser-id (next-browser-id)]
    (swap! browsers assoc browser-id browser)
    browser-id))

(defn boot-driver
  ([type]
   (boot-driver type {}))
  ([type opt]
   (let [browser (eta/boot-driver type opt)
         browser-id (next-browser-id)]
     (swap! browsers assoc browser-id browser)
     browser-id)))

(def firefox (partial boot-driver :firefox))
(def edge    (partial boot-driver :edge))
(def chrome  (partial boot-driver :chrome))
(def phantom (partial boot-driver :phantom))
(def safari  (partial boot-driver :safari))

(defn chrome-headless
  ([]
   (chrome-headless {}))
  ([opt]
   (boot-driver :chrome (assoc opt :headless true))))

(defn firefox-headless
  ([]
   (firefox-headless {}))
  ([opt]
   (boot-driver :firefox (assoc opt :headless true))))

(defmacro def-etaoin
  ([name] `(def-etaoin ~name false))
  ([name ?return] `(def-etaoin ~name ~?return "etaoin.api"))
  ([name return? ns-str]
   `(defn ~name [browser-id# ~'& args#]
      (let [browser# (get @browsers browser-id#)]
        (let [ret# (apply ~(symbol ns-str (str name)) browser# args#)]
          (if ~return?
            ret#
            browser-id#))))))

(def-etaoin go)
(def-etaoin wait)
(def-etaoin wait-exists)
(def-etaoin wait-absent)
(def-etaoin wait-invisible)
(def-etaoin wait-visible)
(def-etaoin wait-running)
(def-etaoin wait-has-class)
(def-etaoin wait-has-text)
(def-etaoin wait-has-alert)
(def-etaoin get-url true)
(def-etaoin get-title true)
(def-etaoin has-text? true)
(def-etaoin exists? true)
(def-etaoin visible? true)
(def-etaoin back)
(def-etaoin forward)
(def-etaoin refresh)
(def-etaoin quit)
(def-etaoin stop-driver)
(def-etaoin disconnect-driver)
(def-etaoin screenshot-element)
(def-etaoin screenshot)
(def-etaoin submit)
(def-etaoin upload-file)
(def-etaoin clear)
(def-etaoin clear-el)
(def-etaoin fill-human)
(def-etaoin fill-human-el)
(def-etaoin fill-multi)
(def-etaoin fill)
(def-etaoin fill-active)
(def-etaoin fill-el)
(def-etaoin click)
(def-etaoin click-visible)
(def-etaoin js-execute true)
(def-etaoin get-element-attr true)
(def-etaoin get-element-text true)

(def-etaoin expand true "etaoin.query")

(def syms '[boot-driver chrome firefox edge phantom safari
            chrome-headless firefox-headless
            quit stop-driver
            disconnect-driver
            go wait
            wait-absent wait-invisible
            wait-visible wait-running wait-has-class
            wait-has-text wait-has-alert wait-exists
            get-url get-title has-text?
            exists?
            visible?
            back forward refresh
            screenshot-element screenshot
            submit upload-file
            clear clear-el
            fill-human fill-human-el fill-multi fill fill-active fill-el
            click click-visible
            js-execute
            get-element-attr get-element-text])

(def query-syms '[expand])

(def lookup
  (merge (zipmap (map (fn [sym]
                       (symbol "pod.babashka.etaoin"
                               (name sym)))
                     syms)
                 (map resolve syms))
         (zipmap (map (fn [sym]
                        (symbol "pod.babashka.etaoin.query"
                                (name sym)))
                      query-syms)
                 (map resolve query-syms))))

(def describe-map
  (walk/postwalk
   (fn [v]
     (if (ident? v) (name v)
         v))
   `{:format :edn
     :namespaces [{:name pod.babashka.etaoin.keys
                   :vars [{:name enter :code "(def enter \\uE007)"}]}
                  {:name pod.babashka.etaoin.query
                   :vars ~(mapv (fn [sym]
                                  {:name sym})
                                query-syms)}
                  {:name pod.babashka.etaoin
                   :vars ~(mapv (fn [sym]
                                  {:name sym})
                                syms)}]
     :opts {:shutdown {}}}))

(debug describe-map)

; The ex-data may have [:driver :process] (a java.lang.Process)
; or [:predicate] (a clojure.lang.AFunction), either of which render as #object
; which will break when the other end tries to read it.
; In case there is anything else just fix the representation of anything without
; its own print-method.
(defmethod print-method Object [v ^java.io.Writer w]
  (.write w (pr-str {:type (type v)
                     :str  (str v)})))

(defn -main [& _args]
  (loop []
    (let [message (try (read)
                       (catch java.io.EOFException _
                         ::EOF))]
      (when-not (identical? ::EOF message)
        (let [op (get message "op")
              op (read-string op)
              op (keyword op)
              id (some-> (get message "id")
                         read-string)
              id (or id "unknown")]
          (case op
            :describe (do (write describe-map)
                          (recur))
            :invoke (do (try
                          (let [var (-> (get message "var")
                                        read-string
                                        symbol)
                                args (get message "args")
                                args (read-string args)
                                args (edn/read-string args)]
                            (if-let [f (lookup var)]
                              (let [value (pr-str (apply f args))
                                    reply {"value" value
                                           "id" id
                                           "status" ["done"]}]
                                (write reply))
                              (throw (ex-info (str "Var not found: " var) {}))))
                          (catch Throwable e
                            (debug e)
                            (let [reply {"ex-message" (ex-message e)
                                         "ex-data" (-> e
                                                       (ex-data)
                                                       ; Rename :type to preserve it
                                                       ; so we can use :type for class.
                                                       (as-> data
                                                             (assoc data
                                                                    :etaoin/type
                                                                    (:type data)))
                                                       (assoc :type (class e))
                                                       pr-str)
                                         "id" id
                                         "status" ["done" "error"]}]
                              (write reply))))
                        (recur))
            :shutdown (System/exit 0)
            (do
              (let [reply {"ex-message" "Unknown op"
                           "ex-data" (pr-str {:op op})
                           "id" id
                           "status" ["done" "error"]}]
                (write reply))
              (recur))))))))
