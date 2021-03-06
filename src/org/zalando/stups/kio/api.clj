; Copyright 2015 Zalando SE
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns org.zalando.stups.kio.api
  (:require [org.zalando.stups.friboo.system.http :refer [def-http-component]]
            [org.zalando.stups.kio.sql :as sql]
            [ring.util.response :refer :all]
            [org.zalando.stups.friboo.ring :refer :all]
            [org.zalando.stups.friboo.log :as log]))

; define the API component and its dependencies
(def-http-component API "api/kio-api.yaml" [db])

(def default-http-configuration
  {:http-port 8080})

;; applications

(defn read-applications [{:keys [search]} _ db]
  (if (nil? search)
    (do
      (log/debug "Read all applications.")
      (-> (sql/read-applications {} {:connection db})
          (response)
          (content-type-json)))
    (do
      (log/debug "Search in applications with term %s." search)
      (-> (sql/search-applications {:searchquery search} {:connection db})
          (response)
          (content-type-json)))))

(defn read-application [{:keys [application_id]} _ db]
  (log/debug "Read application %s." application_id)
  (-> (sql/read-application
        {:id application_id}
        {:connection db})
      (single-response)
      (content-type-json)))

(defn create-or-update-application! [{:keys [application application_id]} _ db]
  (sql/create-or-update-application!
    (merge application {:id application_id})
    {:connection db})
  (log/audit "Created/updated application %s using data %s." application_id application)
  (response nil))

;; versions

(defn read-versions-by-application [{:keys [application_id]} _ db]
  (log/debug "Read all versions for application %s." application_id)
  (-> (sql/read-versions-by-application
        {:application_id application_id}
        {:connection db})
      (response)
      (content-type-json)))

(defn read-version-by-application [{:keys [application_id version_id]} _ db]
  (log/debug "Read version %s of application %s." version_id application_id)
  (-> (sql/read-version-by-application
        {:id             version_id
         :application_id application_id}
        {:connection db})
      (single-response)
      (content-type-json)))

(defn create-or-update-version! [{:keys [application_id version_id version]} _ db]
  (sql/create-or-update-version!
    (merge version {:id             version_id
                    :application_id application_id})
    {:connection db})
  (log/audit "Created/updated version %s for application %s using data %s." version_id application_id version)
  (response nil))

;; approvals

(defn read-approvals-by-version [{:keys [application_id version_id]} _ db]
  (log/debug "Read approvals for version %s of application %s." version_id application_id)
  (-> (sql/read-approvals-by-version
        {:version_id     version_id
         :application_id application_id}
        {:connection db})
      (response)
      (content-type-json)))

(defn approve-version! [{:keys [application_id version_id approval]} _ db]
  (sql/approve-version!
    (merge approval {:version_id     version_id
                     :application_id application_id
                     ; TODO set correct username from authn
                     :user_id        "TODO-FIXME"})
    {:connection db})
  (log/audit "Approved version %s for application %s." version_id application_id)
  (response nil))
