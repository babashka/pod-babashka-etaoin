(require '[babashka.pods :as pods])

#_(pods/load-pod ["lein" "run" "-m" "pod.babashka.etaoin"])
(pods/load-pod "./pod-babashka-etaoin")
(require '[pod.babashka.etaoin :as eta]
         '[pod.babashka.etaoin.keys :as k])

#_(def driver (eta/firefox))  ;; here, a Firefox window should appear
#_(def driver (eta/chrome))
(def driver (eta/firefox-headless))

;; let's perform a quick Wiki session
(eta/go driver "https://en.wikipedia.org/")
(eta/wait-visible driver [{:id :simpleSearch} {:tag :input :name :search}])

;; search for something
(eta/fill driver {:tag :input :name :search} "Clojure programming language")
(eta/fill driver {:tag :input :name :search} k/enter)
(eta/wait-visible driver {:class :mw-search-results})

;; I'm sure the first link is what I was looking for
(eta/click driver [{:class :mw-search-results} {:class :mw-search-result-heading} {:tag :a}])
(eta/wait-visible driver {:id :firstHeading})

;; let's ensure
(prn (eta/get-url driver)) ;; "https://en.wikipedia.org/wiki/Clojure"

(prn (eta/get-title driver)) ;; "Clojure - Wikipedia"

(prn (eta/has-text? driver "Clojure")) ;; true

;; navigate on history
(eta/back driver)
(eta/forward driver)
(eta/refresh driver)
(prn (eta/get-title driver)) ;; "Clojure - Wikipedia"

;; stops Firefox and HTTP server
(eta/quit driver)
nil
