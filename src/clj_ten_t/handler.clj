(ns clj-ten-t.handler
  (:use compojure.core)
  (:use cheshire.core)
  (:use ring.util.response)
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.java.jdbc :as sql]
            [jdbc.pool.c3p0    :as pool]
            [ring.middleware.json :as middleware]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:gen-class))

(def spec
  (pool/make-datasource-spec
    {:classname "org.postgresql.Driver"
     :subprotocol "postgresql"
     :subname "//localhost:5432/ten_thousand_users_dev"}))

(defn get-all-users []
  (into []
    (sql/query spec ["select id, username, email from users"])))

(defn index []
  (response (get-all-users)))

(defroutes app-routes
  (context "/api/users" []
    (GET  "/" [] (index)))
  (route/not-found "It's alive, alive!"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))

(defn -main []
  (jetty/run-jetty app {:port 4000 :join? false}))
