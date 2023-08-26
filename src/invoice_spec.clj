(ns invoice-spec
  (:require
    [clojure.spec.alpha :as s])
  (:import (java.util Date)))

(use 'clojure.test)
(use 'invoice-item)

(defn not-blank? [value] (-> value clojure.string/blank? not))
(defn non-empty-string? [x] (and (string? x) (not-blank? x)))

(s/def :customer/name non-empty-string?)
(s/def :customer/email non-empty-string?)
(s/def :invoice/customer (s/keys :req [:customer/name
                                       :customer/email]))

;;(s/def :tax/rate double?)
;;(s/def :tax/category #{:iva})
;;(s/def ::tax (s/keys :req [:tax/category :tax/rate]))
;;(s/def :invoice-item/taxes (s/coll-of ::tax :kind vector? :min-count 1))

;;(s/def :invoice-item/price double?)
;;(s/def :invoice-item/quantity double?)
;;(s/def :invoice-item/sku non-empty-string?)

;;(s/def ::invoice-item
  ;;(s/keys :req [:invoice-item/price
   ;;             :invoice-item/quantity
    ;;            :invoice-item/sku
      ;;          :invoice-item/taxes]))

;;(s/def :invoice/issue-date inst?)
;;(s/def :invoice/items (s/coll-of ::invoice-item :kind vector? :min-count 1))

(s/def ::invoice
  (s/keys :req [
                ;;:invoice/issue-date
                :invoice/customer
                ]))


(def invoice_result (invoice-item/invoice (slurp "invoice.json")))
;;(def invoice_result (clojure.edn/read-string (slurp "invoice_new.edn")))

(deftest it_checks_if_is_a_valid_invoice_from_json_file
  (is (s/valid? ::invoice invoice_result))
  (s/explain ::invoice invoice_result)
  )

#_(deftest it_checks_if_is_a_valid_invoice_from_json_file
  (prn "test")
  (prn invoice_result)
  (prn (get-in invoice_result [:customer :name]))
  (prn (s/valid? :customer/name (get-in invoice_result [:customer :name])))
  )

#_(def customer (get invoice_result [:id]))
#_(deftest it_checks_if_is_a_valid_invoice_from_json_file_#
  (prn "test_#")
  (prn invoice_result)
  (prn "customer")
  (prn customer)
  (prn (s/valid? :invoice/customer customer))
  )                                                         ;; paso ok con true

#_(def customer2 (get-in invoice_result [:invoice :customer]))
#_(deftest it_checks_if_is_a_valid_invoice_from_json_file_#_2
  (prn "test_#_2")
  (prn invoice_result)
  (prn "Customer 2")
  (prn customer2)
  (prn "invoice with key")
  (prn (get invoice_result :invoice))
  (prn (s/valid? ::invoice get (invoice_result :invoice)))
  (prn "date")
  (prn (s/valid? :order/date (Date.)))
  (s/explain ::invoice invoice_result)

  )