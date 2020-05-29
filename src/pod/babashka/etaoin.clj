(ns pod.babashka.etaoin
  {:clj-kondo/config '{:lint-as {pod.babashka.etaoin/def-etaoin clojure.core/def}}}
  (:refer-clojure :exclude [read read-string])
  (:require [bencode.core :as bencode]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [etaoin.api :as eta]
            [etaoin.keys :as k]
            [clojure.string :as str])
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

(defn firefox []
  (let [browser (eta/firefox)
        browser-id (next-browser-id)]
    (swap! browsers assoc browser-id browser)
    browser-id))

(defmacro def-etaoin
  ([name] `(def-etaoin ~name false))
  ([name return?]
   `(defn ~name [browser-id# ~'& args#]
      (let [browser# (get @browsers browser-id#)]
        (let [ret# (apply ~(symbol "etaoin.api" (str name)) browser# args#)]
          (if ~return?
            ret#
            browser-id#))))))

(def-etaoin go)
(def-etaoin wait-visible)
(def-etaoin fill)
(def-etaoin click)
(def-etaoin get-url true)
(def-etaoin get-title true)
(def-etaoin has-text? true)
(def-etaoin back)
(def-etaoin forward)
(def-etaoin refresh)

(defn quit [browser-id]
  (eta/quit (get @browsers browser-id))
  browser-id)

(def lookup {'pod.babashka.etaoin/firefox firefox
             'pod.babashka.etaoin/go go
             'pod.babashka.etaoin/wait-visible wait-visible
             'pod.babashka.etaoin/fill fill
             'pod.babashka.etaoin/click click
             'pod.babashka.etaoin/get-url get-url
             'pod.babashka.etaoin/get-title get-title
             'pod.babashka.etaoin/has-text? has-text?
             'pod.babashka.etaoin/back back
             'pod.babashka.etaoin/forward forward
             'pod.babashka.etaoin/refresh refresh
             'pod.babashka.etaoin/quit quit})

(def describe-map
  (walk/postwalk
   (fn [v]
     (if (ident? v) (name v)
         v))
   `{:format :edn
     :namespaces [{:name pod.babashka.etaoin.keys
                   :vars [{:name enter :code "(def enter \\uE007)"}]}
                  {:name pod.babashka.etaoin
                   :vars [{:name firefox}
                          {:name go}
                          {:name wait-visible}
                          {:name fill}
                          {:name click}
                          {:name get-url}
                          {:name get-title}
                          {:name has-text?}
                          {:name back}
                          {:name forward}
                          {:name refresh}
                          {:name quit}]}]
     :opts {:shutdown {}}}))

(debug describe-map)

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
                                         "ex-data" (pr-str
                                                    (assoc (ex-data e)
                                                           :type (class e)))
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