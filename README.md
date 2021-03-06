# defnw

A Clojure library that defines `defnw` and `defnw-`, macros that replace
`defn` and `defn-` so as to handle special cases in the pre-post map.

[![Build Status](https://travis-ci.org/gabrielash/defnw.svg?branch=master)](https://travis-ci.org/gabrielash/defnw)

## Rationale 

A common pattern is to test arguments for certain conditions that preclude the execution of the function's main purpose.

```
    (defn do-that 
      [ arg-1 arg-2 ]
        (cond (that-is-inapropriate? arg-1)
              special-case-return-value
              (that-is-impossible-with? arg-2)
              special-case-return-value-2
              :else 
              (actually-do-that arg-1 arg-2)))
```

`defnw` increases readability by limiting the body of the function to
its main purpose (the `:else` clause), moving argument testing to the
pre-post map.

This is partiallly similar to `pre`. But, unlike conditions in the `:pre` 
vector of the pre-post map, conditions in the `:cond` vector do not trigger
assert failures. Each condition must be followed by an expression to be 
evaluated and returned when the condition is met.

Local bindings can be set for the whole function,  including the `:cond`vector, in a `:let` vector.


## Usage

### Include in project

[![Clojars Project](https://img.shields.io/clojars/v/gabrielash/defnw.svg)](https://clojars.org/gabrielash/defnw)

### Example

```

  (ns my-namespace 
    (:require [gabrielash.defnw.core :refer [defnw]]))

  (defnw add-display-name
    [{:keys [ first-name last-name name-withwheld? ] :as person}]

      {:pre [(string? last-name)
              (seq last-name)]
       :cond [name-withheld? person]}

      (assoc person
             :display-name
             (str first-name " " last-name)))

```

### Caveats

 * if you're redefining `cond`, `defn`, or `do`, well ...
 * :cond/:let with a nil value are ignored
 * This is a learning exercise; use at your own risk


## License

Copyright © 2020 Gabriel Ash

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.

This code is provided as is, without any guarantee whatsoever.
