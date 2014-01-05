(defproject async-play "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1820"] 
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]]
  :profiles {:dev {:source-paths ["dev"]}}
  :plugins [[lein-cljsbuild "0.3.2"]]
  :repositories {
                 "sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"
                 }
  :cljsbuild
  {:builds
   [{:source-paths ["src/async_play/web"]
     :compiler {:output-to "resources/public/js/hello.js"
                :optimizations :whitespace
                :pretty-print true}}]})
