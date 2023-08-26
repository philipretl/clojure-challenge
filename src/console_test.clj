(ns console_test
  (:require [invoice-item])
  (:require [clojure.data.json :as json]
            [invoice-spec])
  )

(use 'clojure.test)

;; First Challenge

(def invoice (clojure.edn/read-string (slurp "invoice.edn")))
(def invoice-items (get invoice :invoice/items))

(prn "Invoice items filtered by conditions")
(prn (invoice-item/filter-invoices invoice-items) )


;; Second Challenge

(def invoice_json_path  (slurp "invoice.json"))

(prn "Invoice load from json file")
(prn (invoice-item/invoice invoice_json_path))


(run-tests 'invoice-spec)

