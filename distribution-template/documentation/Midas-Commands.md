# Midas
***On-the-fly Schema Migration Tool for MongoDB***
##Commands
As a general guiding principle, all the commands below are fail-safe.  That is
to say, if an input field expected by a command is not present, then that transformation
is not applicable.  If the output field does not exist, then it is created.
In case if the output field exists, then its over-written.

###What happens when a Command fails to apply itself?
####Scenario 1
In case, if there is a failure to apply a particular transformation, Midas
will inject against that field an exception sub-document.  Say for some reason, the
following split transformation failed:

 `db.collection.split('field', 'regex', '{ "firstField": "$1", "secondField": "$2"}')`

then for each output field, Midas will introduce an exception sub-document instead of a
value.

 `{
    "firstField": { _errmsg: "exception: blah, blah, ..."},
    "secondField": { _errmsg: "exception: blah, blah, ..."
  }`

####Secanrio 2
In an event one of the transformations in a series of transformations fail, then we
have a case of partially transformed document.  In such a case, Midas will, at
document level inject a field _errMsg field indicating that this is a partially
transformed document.

 `{
    ...
    _errmsg: "exception: blah, blah, ...",
    ...
  }`


##Commands Reference
###The use command
Use a particular database and that sets the context to that DB, so that all commands
appearing after it will look for collections within that DB.  This behavior is
same as the use command used on Mongo Shell.

####Syntax
`use <db>`

####Examples
`use transactions`

Set context to users db
`use users`


###The add command                                                                                         
Add single or multiple fields that can nested or add a nested document.                                      
                                                                                                    
####Syntax                                                                                             
`db.collection.add('{ "field" : "value" [, ...]}')`                                            
`db.collection.add("{ 'field' : 'value' [, ...]}")`                                            
                                                                                                    
Command argument can either be wrapped in a single or a double quoted String.                       
Like JavaScript, you can use quotes inside a string, as long as they don't match the                
quotes surrounding the string                                                                       
                                                                                       
####Examples                                                                                           
`db.collection.add('{ "fieldOne" : "valueOne"}')`                                              
`db.collection.add('{ "fieldOne" : "valueOne", "nested.fieldTwo" : 2, ... }')`                 
`db.collection.add("{ 'fieldOne' : 'valueOne', 'nested' : { 'fieldTwo' : 2}, ... }")`          
`db.collection.add("{ 'arrayField' : ['one', 'two', ...] }")`                                  
                                                                                                    
Add age field with default value 0 to all documents that do not have one
`db.customers.add('{"age" : 0 }')`

###The remove command
Remove single or multiple fields

####Syntax
  `db.collection.remove('["fieldOne", "nested.fieldTwo", "arrayField", ...]')`
  `db.collection.remove("['fieldOne', 'nested.fieldTwo', 'arrayField', ...]")`
 
####Examples
`db.customers.remove("['age']")`
`db.orders.remove("['dispatch.status']")`

###The copy command
Copies value from a field to another, if the target field does not exist, create one.
Source and target fields can be nested.

####Syntax
`db.collection.copy('fromFieldName', 'toFieldName')`
`db.collection.copy("fromFieldName", "toFieldName")`

####Examples
`use users`
`db.customers.copy('pin', 'address.zip')`

###The merge command
Merge the given source fields into a target field using a user-defined separator.  Separator can be any punctuations, symbols, spaces, alphanumeric string

####Syntax
`db.collection.merge('["fieldOne", "fieldTwo", ...]', 'usingSeparator', 'targetField')`
`db.collection.merge("['fieldOne', 'fieldTwo', ...]", "usingSeparator", "targetField")`

####Examples
`use users`
`db.customers.merge('["title", "lname", "fname"]', ' ', 'details.name')`

###The split command
Split a field in to multiple fields using a user-defined regex.  The regex must contain groups that can be extracted and 
values extracted from there will be applied to the newly created fields.

####Syntax
`db.collection.split('field', 'regex', '{ "firstField": "$1", "secondField": "$2", ... }')`
`db.collection.split("field", "regex", "{ 'firstField': '$1', 'secondField': '$2', ... }")`

####Examples
`use users`
`db.customers.split('details.name', '^(Mr|Mrs|Ms|Miss) ([a-zA-Z]+) ([a-zA-Z]+)$', '{"title": "$1", "fullName": { "firstName": "$2", "lastName": "$3" }}')`

###The transform command
Transform a field by using a built-in transformation functions.  Transformation function is a built-in function that takes in values or input fields 
or other built-in functions and returns the evaluation result by setting it on the output field.  If the output field does not exist, it is created, 
else its value is over-written.

####Syntax
`db.collection.transform('outField', '{ $transformationFunction : [arg0, arg1, ...] }')`
`db.collection.transform("outField", "{ $transformationFunction : [arg0, arg1, ...] }")`

####Arithmetic Transformation Functions
#####The `$add` function 
Takes an array of one or more numbers and adds them together, returning the sum.
#####Syntax
`db.things.transform("outputFieldName", "{ $add: [arg0, arg1, ...] }")`

#####The `$multiply` function 
Takes an array of one or more numbers and multiples them, returning the resulting product.
#####Syntax
`db.things.transform("outputFieldName", "{ $multiply: [arg0, arg1, ...] }")`

#####The `$divide` function 
Takes an array that contains a pair of numbers and returns the value of the first number divided by the second number.
It ignores more than 2 arguments.
#####Syntax
`db.things.transform("outputFieldName", "{ $divide: [arg0, arg1] }")`

#####The `$subtract` function 
Takes an array that contains a pair of numbers and subtracts the second from the first, returning their difference.
It ignores more than 2 arguments.
####Syntax
`db.things.transform("outputFieldName", "{ $subtract: [arg0, arg1] }")`

#####The `$mod` function 
Takes an array that contains a pair of numbers and returns the remainder of the first number divided by the second number.
#####Syntax
`db.things.transform("outputFieldName", "{ $mod: [arg0, arg1] }")`

###Examples of Arithmetic Transformation Functions
`use users`

`//Increment age by 1`
`db.customers.transform('age', '{ $add: ["$age", 1] }')`

`//Decrement age by 1`
`db.customers.transform('age', '{ $subtract: ["$age", 1] }')`

`//recursive example - add 1 and subtract 1 after in the same transformation`
`db.customers.transform('age', '{ $subtract: [{ $add: ["$age", 1]}, 1] }')`

`// make age twice`
`db.customers.transform('age', '{ $multiply: ["$age", 2] }')`

`// make age 1/3rd`
`db.customers.transform('age', '{ $divide: ["$age", 3] }')`

`// age % 3`
`db.customers.transform("age", "{ $mod: ['$age', 3]}")`

####String Transformation Functions

#####The `$concat` function 
Takes an array of one or more string and concats them together, returning the concatenated string.

#####Syntax
`db.things.transform("outputFieldName", "{ $concat: ["$name", "-" , ...] }")`

#####The `$toLower` function 
Takes single argument (constant or field) and converts it to lower case.

#####Syntax
`db.things.transform("outputFieldName", "{ $toLower: "$name" }")`

#####The `$toUpper` function 
Takes single argument (constant or field) and converts it to upper case.

#####Syntax
`db.things.transform("outputFieldName", "{ $toUpper: "$name" }")`

###Examples of String Transformation Functions
`use catalog`

`db.items.transform('itemName', '{ $concat: ["item - ", "$itemName"] }')`
`db.items.transform('itemName', '{ $toUpper: "$itemName" }')`
`db.items.transform('description', '{ $toLower: "$itemName" }')`
`db.items.transform('description', '{ $toUpper: { $concat: ["Description for ", "$description"] } }')`


####Date Transformation Functions

#####The `$date` function
Takes an array of two arguments. First one is the date format (the standard
[Java Date format applies](http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html)) and the second argument is the date in the form
of String or could come from other fields as well

#####Syntax
`db.things.transform("outputFieldName", "{ $date: [format, value] }")`

###Examples of Date Transformation Functions
`use transactions`

`db.orders.transform("executionDate", '{ $date: ["MMM dd, yyyy HH:mm", "Jun 23, 1912 00:00"]}')`
`db.orders.transform("dispatch.date", "{ $date : ['dd-MMM-yy', '18-Aug-87']}")`
