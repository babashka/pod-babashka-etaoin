(ns pod.babashka.etaoin-test
  (:require [babashka.pods :as pods]
            [clojure.test :refer [deftest is]]))

(if (= "native" (System/getenv "POD_TEST_ENV"))
  (do (pods/load-pod "./pod-babashka-etaoin")
      (println "Testing native version"))
  (do (pods/load-pod ["lein" "run" "-m" "pod.babashka.etaoin"])
      (println "Testing JVM version")))

(require '[pod.babashka.etaoin :as eta]
         '[pod.babashka.etaoin.keys :as k])

(deftest etaoin-test
  (let [driver (eta/firefox-headless)]
    ;; let's perform a quick Wiki session
    (eta/go driver "https://en.wikipedia.org/")
    (eta/wait-visible driver [{:id :simpleSearch} {:tag :input :name :search}])

    (is (= "Main Page"
           (eta/get-element-text driver {:href "/wiki/Main_Page"})))
    (is (= "/wiki/Main_Page"
           (eta/get-element-attr driver {:href "/wiki/Main_Page"} :href)))

    ;; search for something
    (eta/fill driver {:tag :input :name :search} "Clojure programming language")
    (eta/fill driver {:tag :input :name :search} k/enter)
    (eta/wait-visible driver {:class :mw-search-results})

    ;; I'm sure the first link is what I was looking for
    (eta/click driver [{:class :mw-search-results} {:class :mw-search-result-heading} {:tag :a}])
    (eta/wait-visible driver {:id :firstHeading})

    (is (= "https://en.wikipedia.org/wiki/Clojure"
           (eta/get-url driver)))

    (is (= "Clojure - Wikipedia" (eta/get-title driver)))

    (is (eta/has-text? driver "Clojure"))

    ;; navigate on history
    (eta/back driver)
    (eta/forward driver)
    (eta/refresh driver)
    (is (= "Clojure - Wikipedia" (eta/get-title driver)))

    (is (= true
           (eta/exists? driver {:tag "html"})))
    (is (= true
           (not (eta/exists? driver {:css "marquee.hopefully-not-present"}))))

    (is (= true
           (eta/visible? driver {:tag "html"})))
    (is (= true
           (not (eta/visible? driver {:css "marquee.hopefully-not-present"}))))

    (is (= driver
           (eta/wait-absent driver :should-not-be-found)))
    (is (= driver
           (eta/wait-invisible driver :should-not-be-found)))

    (eta/quit driver)))
