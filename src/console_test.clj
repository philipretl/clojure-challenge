(ns console_test
  (:require [invoice-item])
  (:require [clojure.data.json :as json]
            [invoice-spec])
  )

(use 'clojure.test)

;;First Challenge
(println)
(println "####### First Challenge")
(prn "Invoice items filtered by conditions")
(println)
(def invoice (clojure.edn/read-string (slurp "invoice.edn")))
(def invoice-items (get invoice :invoice/items))
(println (invoice-item/filter-invoices invoice-items))

;;Second Challenge
(println)
(println "####### Second Challenge")
(println "Invoice load from json file and map to the defined structure")
(println)
(def invoice_json_path (slurp "invoice.json"))
(prn (invoice-item/invoice invoice_json_path))

;; Third Challenge
(println)
(println "####### Third Challenge")
(println "Make the suite test for the subtotal function")
(println)
(println "With discount - subtotal: " (invoice-item/subtotal {:precise-quantity 10 :precise-price 10 :discount-rate 20}))
(println "Without discount - subtotal: " (invoice-item/subtotal {:precise-quantity 10 :precise-price 10}))
(println "Negatives values - subtotal: " (invoice-item/subtotal {:precise-quantity -1 :precise-price -2 :discount-rate -2}))

;; Run test
(println)
(println "####### Run Test Suite")
(run-tests 'invoice-spec)

(println)
(println "Challenge Completed - by Andres Felipe Vega Noguera - afelipe.vega@gmail.com")
(println)



