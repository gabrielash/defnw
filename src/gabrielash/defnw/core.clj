;  Copyright (C) 2020 Gabriel Ash

; This program and the accompanying materials are made available under the
; terms of the Eclipse Public License 2.0 which is available at
; http://www.eclipse.org/legal/epl-2.0 .
; This code is provided as is, without any guarantee whatsoever.

(ns gabrielash.defnw.core
  "`defnw`/`defnw-` are substitutes for
  [defn](https://clojuredocs.org/clojure.core/defn)
  /[defn-](https://clojuredocs.org/clojure.core/defn-)  
  but add an option for `:cond` and `:let` vectors in the `:pre` map,  
  useful for handling special cases outside the function's body."
  (:require [net.cgrand.seqexp :as se]
            [gabrielash.misc.shorts :refer :all]
            ))

(def ^:private args+pp+body
  "seqex match for function body declaration based on seqex documentation" 
  (se/cat
   (se/as :argv vector?)
   (se/? (se/as :pp map?))
   (se/as :body (se/* se/_))))

(def ^:private  fn-def
  "seqex match for full function definition based on seqex documentation"
  (se/cat
   (se/as :name symbol?)
   (se/? (se/as :docstring string?))
   (se/? (se/as :meta map?))
   (se/|
    (se/as :single args+pp+body)
    (se/as :multiple
           (se/+ (partial se/exec
                          args+pp+body))))))


(defn- assert-forms-ok? [kword target]
  (assert (vector? target)        (str "defnw -> " kword " value must be a vector!"))
  (assert (!! empty? target)      (str "defnw -> " kword " vector cannot be empty!"))
  (assert (even? (count target))  (str "defnw -> " kword " requires even number of exprs!"))
  true)


(defn- wrap-in-let
  [letv body]
  #_(println "### " letv body)
  
  (when letv
   (assert-forms-ok? :let letv))
  
  (if letv
   (seq ['let letv body])
    body))


(defn- wrap-in-cond
  [condv body]
  #_(println ";;; " condv)
   
  (when condv 
   (assert-forms-ok? :cond condv))      
  
  (if condv
   (-> condv
       (conj :else body)
       (seq)
       (conj 'cond))
    body))


(defn- transform-body
  "apply pre-post transformations to body if given"
  [body]
  (let [match-map (se/exec args+pp+body body)
        pp (first (:pp match-map))
        argv (first (:argv match-map))
        body (cons 'do (:body match-map))]

    #_(do 
        (println "<<< " match-map)
        (println ">   " pp)
        (println "[]> " argv)
        (println "()> " body))

    (seq
     (if (!! empty? pp)
       [argv pp (->> body
                     (wrap-in-cond (:cond pp))
                     (wrap-in-let (:let pp)))]
       [argv body]))))


(defmacro -defnw
  "internal macro implementation for defn/defn- 
   alternatives that allows definition of 
     conditions and return values for special cases 
     inside the pre-post map"
  [call-name & definition]
  (let [match-map (se/exec
                   fn-def definition)
        {:keys [name docstring meta]} match-map
        preamble (->> [name docstring meta]
                      (filter identity)
                      (map first)
                      (concat `(~call-name)))
        body (cond (contains? match-map :single)
                   [(:single match-map)]
                   (contains? match-map :multiple)
                   (:multiple match-map)
                   :else
                   (throw (ex-info "malformed definition: missing body"
                                   {:data match-map})))
        macroexpanded (doall
                        (concat preamble
                          (map transform-body body)))]

        #_(do 
            (println "=== " name docstring meta)
            (println "*** " preamble)
            (println match-map)
            (println "<*  " macroexpanded " *>"))

    macroexpanded))


(defmacro defnw 
  "defn alternative that allows definition of 
     conditions and return values for special cases 
     inside the pre-post map"
  [& definition] 
  `(-defnw defn  ~@definition) )

(defmacro defnw-
  "defn- alternative that allows definition of 
     conditions and return values for special cases 
     inside the pre-post map"
  [& definition]
  `(-defnw defn-  ~@definition))
