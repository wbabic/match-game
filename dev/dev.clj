(ns dev
  (:require [cljs.repl]
            [cljs.repl.browser :refer [repl-env]]))

(comment
(defn cljs-repl
  "start a clujrescript repl"
  []
  (pb/cljs-repl :repl-env
                (doto (repl-env)
                  cljs.repl/-setup))
  (println "CLJS REPL launched. Refresh http://localhost:3000/frech.html"))

   (do
    (require 'cljs.repl.browser)
    (cemerick.piggieback/cljs-repl
     :repl-env
     (doto (cljs.repl.browser/repl-env :port 9000)
       cljs.repl/-setup)))
   )
