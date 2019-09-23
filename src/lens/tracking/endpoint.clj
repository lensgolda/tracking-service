(ns lens.tracking.endpoint
  (:require [com.stuartsierra.component :as component]
            [reitit.ring :as ring]
            [reitit.http :as http]
            [reitit.http.interceptors.parameters :as parameters]
            ;; [reitit.http.interceptors.multipart :as multipart]
            [reitit.http.interceptors.muuntaja :as muuntaja]
            [reitit.interceptor.sieppari :as sieppari]
            [reitit.swagger :as swagger]
            [reitit.http.coercion :as httpc]
            [reitit.coercion.spec :as coercion]
            [reitit.swagger-ui :as swagger-ui]
            [muuntaja.core :as m]
            [lens.tracking.handler :as handler]
            [lens.tracking.interceptors :as i]
            [lens.tracking.spec :as spec]))

(defn routes
  [{<redis> :cache}]
  (http/ring-handler
    (http/router
      [["/ping" {:get {:summary "Pings service availability"
                       :responses {200 {:body nil}}
                       :handler (fn [_]
                                  {:status 200})}}]
       ["/geoadd" {:post {:summary    "Adds the specified geospatial items (latitude, longitude, name)"
                          :parameters {:body {:position ::spec/position :id ::spec/id}}
                          :responses  {200 {:body {:total int? :id string?}}}
                          :interceptors [(i/with-component <redis>)]
                          :handler    handler/geoadd}}]
       ["/geodist" {:get {:summary      "Return the distance between two members in the geospatial index"
                          :parameters   {:query {:id1 string? :id2 string? :unit string?}}
                          :responses    {200 {:body {:distance string?}}}
                          :interceptors [(i/with-component <redis>)]
                          :handler      handler/geodistance}}]
       ["/geopos" {:get {:summary      "Return the positions (longitude,latitude) of all the specified members of the geospatial index represented by the sorted set at key."
                         :parameters   {:query {:id string?}}
                         :responses  {200 {:body {:position string?}}}
                         :interceptors [(i/with-component <redis>)]
                         :handler      handler/geopos}}]
       ["/georadius" {:get {:summary      "Return the members of a sorted set populated with geospatial information using GEOADD, which are within the borders of the area specified with the center location and the maximum distance from the center (the radius)."
                            :parameters   {:query {:lat ::spec/lat :lng ::spec/lng :unit string?}}
                            :responses  {200 {:body {:objects vector?}}}
                            :interceptors [(i/with-component <redis>)]
                            :handler      handler/geopos}}]
       ["/swagger.json"
        {:get {:no-doc true
               :swagger {:info {:title "tracking API"
                                :description ""}
                         :basePath "/"}
               :handler (swagger/create-swagger-handler)}}]]

      {:data {:coercion     coercion/coercion
              :muuntaja     m/instance
              :interceptors [(parameters/parameters-interceptor)
                             (muuntaja/format-interceptor)
                             (httpc/coerce-exceptions-interceptor)
                             (httpc/coerce-response-interceptor)
                             (httpc/coerce-request-interceptor)]}})
    (ring/routes
      (swagger-ui/create-swagger-ui-handler {:path "/api"})
      (ring/create-default-handler))
    {:executor sieppari/executor
     :inject-match false
     :inject-router false}))

(defrecord Endpoint [routes-fn]
  component/Lifecycle
  (start [this]
    (assoc this :routes (routes-fn this)))
  (stop [this]
    (assoc this :routes nil)))

(defn create
  []
  (->Endpoint routes))