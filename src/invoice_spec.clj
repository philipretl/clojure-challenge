(ns invoice-spec
  (:require
    [clojure.spec.alpha :as s])
  )

(use 'clojure.test)
(use 'invoice-item)

(defn not-blank? [value] (-> value clojure.string/blank? not))
(defn non-empty-string? [x] (and (string? x) (not-blank? x)))

(s/def :customer/name non-empty-string?)
(s/def :customer/email non-empty-string?)
(s/def :invoice/customer (s/keys :req [:customer/name
                                       :customer/email]))

(s/def :tax/rate double?)
(s/def :tax/category #{:iva})
(s/def ::tax (s/keys :req [:tax/category :tax/rate]))
(s/def :invoice-item/taxes (s/coll-of ::tax :kind vector? :min-count 1))

(s/def :invoice-item/price double?)
(s/def :invoice-item/quantity double?)
(s/def :invoice-item/sku non-empty-string?)

(s/def ::invoice-item
  (s/keys :req [:invoice-item/price
                :invoice-item/quantity
                :invoice-item/sku
                :invoice-item/taxes
                ]))

(s/def :invoice/issue-date inst?)
(s/def :invoice/items (s/coll-of ::invoice-item :kind vector? :min-count 1))

(s/def ::invoice
  (s/keys :req [:invoice/issue-date
                :invoice/customer
                :invoice/items
                ]))


(def invoice_result (invoice-item/invoice (slurp "invoice.json")))

(deftest invoice_map
  (testing "It checks if the invoice is mapped correctly"
    (is (s/valid? ::invoice invoice_result))
    )
  )


;; Subtotal Unit Test
(s/def ::subtotal
  (s/keys :req [
                :subtotal/discount
                ])
  )
(s/def :subtotal/discount double?)

(deftest subtotal_test
  (testing "It checks the values that the function subtotal calculate"
    (is (= 16.0 (invoice-item/subtotal {:precise-quantity 2 :precise-price 10 :discount-rate 20})))
    (is (= 20.0 (invoice-item/subtotal {:precise-quantity 2 :precise-price 10})))
    (is ::subtotal (invoice-item/subtotal {:precise-quantity 2 :precise-price 10 :discount-rate 20}))

    )

  (testing "It checks the values when the quantity is zero"
    (is (= 0.0 (invoice-item/subtotal {:precise-quantity 0 :precise-price 10 :discount-rate 20})))
    )

  (testing "It checks the values when the price is zero"
    (is (= 0.0 (invoice-item/subtotal {:precise-quantity 2 :precise-price 0 :discount-rate 20})))
    )

  (testing "It checks the values when the quantity or price or discount rate is negative value"
    (is (= 0.0 (invoice-item/subtotal {:precise-quantity -2 :precise-price -2 :discount-rate -20})))
    )

  )