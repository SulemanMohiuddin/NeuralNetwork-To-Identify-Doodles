
CREATE OR REPLACE FUNCTION update_order_total()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE Order1
    SET TotalAmount = (SELECT SUM(i.Price * c.Quantity)
                       FROM Cart c
                       JOIN Item i ON c.ItemID = i.ItemID
                       WHERE c.OrderID = NEW.OrderID)
    WHERE Order1.OrderID = NEW.OrderID;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_order_total_trigger
AFTER INSERT OR UPDATE ON Cart
FOR EACH ROW
EXECUTE FUNCTION update_order_total();



CREATE OR REPLACE FUNCTION auto_assign_delivery_guy()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.Status = 'pending' THEN
        UPDATE DeliveryAssignment
        SET DeliveryGuyID = (SELECT DeliveryGuyID
                             FROM DeliveryGuy
                             WHERE Status = 'available'
                             LIMIT 1)
        WHERE OrderID = NEW.OrderID;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER auto_assign_delivery_guy_trigger
AFTER UPDATE ON Order1
FOR EACH ROW
EXECUTE FUNCTION auto_assign_delivery_guy();




CREATE OR REPLACE FUNCTION update_delivery_status()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.StatusID = (SELECT StatusID FROM DeliveryStatus WHERE StatusName = 'delivered') THEN
        UPDATE DeliveryAssignment
        SET AssignmentDate = CURRENT_TIMESTAMP
        WHERE AssignmentID = NEW.AssignmentID;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_delivery_status_trigger
AFTER UPDATE ON DeliveryAssignment
FOR EACH ROW
EXECUTE FUNCTION update_delivery_status();



CREATE OR REPLACE FUNCTION prevent_duplicate_email()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM User WHERE Email = NEW.Email) THEN
        RAISE EXCEPTION 'Email address already exists';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_duplicate_email_trigger
BEFORE INSERT ON User
FOR EACH ROW
EXECUTE FUNCTION prevent_duplicate_email();



CREATE TABLE ItemBackup (
    BackupID SERIAL PRIMARY KEY,
    ItemID INT,
    ItemName VARCHAR(255),
    Description TEXT,
    Price DECIMAL(10, 2),
    RestaurantID INT,
    ChangeType VARCHAR(20), -- 'INSERT', 'UPDATE', 'DELETE'
    ChangeDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ItemID) REFERENCES Item(ItemID)
);


CREATE OR REPLACE FUNCTION item_backup_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO ItemBackup (ItemID, ItemName, Description, Price, RestaurantID, ChangeType)
        VALUES (NEW.ItemID, NEW.ItemName, NEW.Description, NEW.Price, NEW.RestaurantID, 'INSERT');
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO ItemBackup (ItemID, ItemName, Description, Price, RestaurantID, ChangeType)
        VALUES (OLD.ItemID, OLD.ItemName, OLD.Description, OLD.Price, OLD.RestaurantID, 'DELETE');

        INSERT INTO ItemBackup (ItemID, ItemName, Description, Price, RestaurantID, ChangeType)
        VALUES (NEW.ItemID, NEW.ItemName, NEW.Description, NEW.Price, NEW.RestaurantID, 'UPDATE');
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO ItemBackup (ItemID, ItemName, Description, Price, RestaurantID, ChangeType)
        VALUES (OLD.ItemID, OLD.ItemName, OLD.Description, OLD.Price, OLD.RestaurantID, 'DELETE');
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER item_backup_trigger
AFTER INSERT OR UPDATE OR DELETE ON Item
FOR EACH ROW
EXECUTE FUNCTION item_backup_trigger();





CREATE OR REPLACE FUNCTION CheckAllDeliveryGuysBusy()
RETURNS TRIGGER AS $$
DECLARE
    totalDeliveryGuys INT;
    busyDeliveryGuys INT;
BEGIN
    -- Assuming 10 delivery guys for this example
    totalDeliveryGuys := 10;

    -- Count the number of busy delivery guys
    SELECT COUNT(*) INTO busyDeliveryGuys
    FROM DeliveryGuy
    WHERE Status = 'busy';

    -- Check if all delivery guys are busy
    IF busyDeliveryGuys = totalDeliveryGuys THEN
        -- Trigger your desired action here (e.g., raise an alert, log, etc.)
        RAISE EXCEPTION 'All delivery guys are busy. Cannot accept new assignments.';
    END IF;

    -- If the condition is not met, return the NEW row
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER check_all_delivery_guys_busy
BEFORE INSERT ON DeliveryAssignment
FOR EACH ROW
EXECUTE FUNCTION CheckAllDeliveryGuysBusy();




-- Example of a transaction to place an order
BEGIN;
    -- Insert order details
    INSERT INTO Order1 (CustomerID, OrderDate, Status, TotalAmount)
    VALUES (1, CURRENT_TIMESTAMP, 'pending', 0.00)
    RETURNING OrderID INTO my_order_id;

    -- Insert items into the cart
    INSERT INTO Cart (OrderID, ItemID, Quantity, TotalPrice)
    VALUES (my_order_id, 101, 2, (SELECT Price * 2 FROM Item WHERE ItemID = 101)),
           (my_order_id, 102, 1, (SELECT Price FROM Item WHERE ItemID = 102));

    -- Update total amount in the order
    UPDATE Order1
    SET TotalAmount = (SELECT SUM(TotalPrice) FROM Cart WHERE OrderID = my_order_id)
    WHERE OrderID = my_order_id;

    -- Perform payment (additional logic needed)
    INSERT INTO Payment (OrderID, Amount, PaymentDate)
    VALUES (my_order_id, (SELECT TotalAmount FROM Order1 WHERE OrderID = my_order_id), CURRENT_TIMESTAMP);

COMMIT;



-- Example of a transaction to update order status
BEGIN;
    UPDATE Order1
    SET Status = 'delivered'
    WHERE OrderID = 1;

    -- Additional logic (e.g., trigger) for handling the update and notifying relevant parties

COMMIT;



-- Example of a transaction to add an item to the menu
BEGIN;
    INSERT INTO Item (ItemName, Price, RestaurantID)
    VALUES ('New Item', 15.99, 201)
    RETURNING ItemID INTO my_item_id;

    -- Additional logic (e.g., trigger) for handling the addition and updating related tables

COMMIT;



-- Example of a transaction to assign a delivery guy
BEGIN;
    UPDATE DeliveryAssignment
    SET DeliveryGuyID = 301,
        StatusID = (SELECT StatusID FROM DeliveryStatus WHERE StatusName = 'assigned')
    WHERE AssignmentID = 1;

    -- Additional logic (e.g., trigger) for handling the assignment and notifying relevant parties

COMMIT;


-- Example of a transaction to update delivery guy status
BEGIN;
    UPDATE DeliveryGuy
    SET Status = 'on delivery'
    WHERE DeliveryGuyID = 301;

    -- Additional logic (e.g., trigger) for handling the update and notifying relevant parties

COMMIT;



-- Example of a transaction to cancel an order
BEGIN;
    UPDATE Order1
    SET Status = 'canceled'
    WHERE OrderID = 1;

    -- Remove items from the cart
    DELETE FROM Cart
    WHERE OrderID = 1;

    -- Additional logic (e.g., trigger) for handling the cancellation and notifying relevant parties

COMMIT;


-- Example of a transaction to add a category
BEGIN;
    INSERT INTO Category (CategoryName)
    VALUES ('Desserts')
    RETURNING CategoryID INTO my_category_id;

    -- Additional logic (e.g., trigger) for handling the addition and updating related tables

COMMIT;



-- Example of a transaction to update restaurant location
BEGIN;
    UPDATE Restaurant
    SET Location = 'New Location'
    WHERE RestaurantID = 201;

    -- Additional logic (e.g., trigger) for handling the update and notifying relevant parties

COMMIT;


MONGO------------------------------------------------------



show dbs
db 

Switch Database---------
use <database_name>

Show Collections-------
show collections

 
CRUD
Create--------
db.coll.insertOne({name: "Max"})
db.coll.insertMany([{name: "Max"}, {name:"Alex"}]) // ordered bulk insert
db.coll.insertMany([{name: "Max"}, {name:"Alex"}], {ordered: false}) // unordered bulk insert
db.coll.insertOne({date: ISODate()})
db.coll.insertOne({name: "Max"}, {"writeConcern": {"w": "majority", "wtimeout": 5000}})

Read----------
db.coll.findOne() // returns a single document
db.coll.find()    // returns a cursor - show 20 results - "it" to display more
db.coll.find().pretty()
db.coll.find({name: "Max", age: 32}) // implicit logical "AND".
db.coll.find({date: ISODate("2020-09-25T13:57:17.180Z")})
db.coll.find({name: "Max", age: 32}).explain("executionStats") // or "queryPlanner" or "allPlansExecution"
db.coll.distinct("name")

// Count
db.coll.countDocuments({age: 32}) // alias for an aggregation pipeline - accurate count
db.coll.estimatedDocumentCount()  // estimation based on collection metadata

// Comparison
db.coll.find({"year": {$gt: 1970}})
db.coll.find({"year": {$gte: 1970}})
db.coll.find({"year": {$lt: 1970}})
db.coll.find({"year": {$lte: 1970}})
db.coll.find({"year": {$ne: 1970}})
db.coll.find({"year": {$in: [1958, 1959]}})
db.coll.find({"year": {$nin: [1958, 1959]}})

// Logical
db.coll.find({name:{$not: {$eq: "Max"}}})
db.coll.find({$or: [{"year" : 1958}, {"year" : 1959}]})
db.coll.find({$nor: [{price: 1.99}, {sale: true}]})
db.coll.find({
  $and: [
    {$or: [{qty: {$lt :10}}, {qty :{$gt: 50}}]},
    {$or: [{sale: true}, {price: {$lt: 5 }}]}
  ]
})

// Element
db.coll.find({name: {$exists: true}})
db.coll.find({"zipCode": {$type: 2 }})
db.coll.find({"zipCode": {$type: "string"}})

// Aggregation Pipeline
db.coll.aggregate([
  {$match: {status: "A"}},
  {$group: {_id: "$cust_id", total: {$sum: "$amount"}}},
  {$sort: {total: -1}}
])

// Text search with a "text" index
db.coll.find({$text: {$search: "cake"}}, {score: {$meta: "textScore"}}).sort({score: {$meta: "textScore"}})

// Regex
db.coll.find({name: /^Max/})   // regex: starts by letter "M"
db.coll.find({name: /^Max$/i}) // regex case insensitive

// Array
db.coll.find({tags: {$all: ["Realm", "Charts"]}})
db.coll.find({field: {$size: 2}}) // impossible to index - prefer storing the size of the array & update it
db.coll.find({results: {$elemMatch: {product: "xyz", score: {$gte: 8}}}})

// Projections
db.coll.find({"x": 1}, {"actors": 1})               // actors + _id
db.coll.find({"x": 1}, {"actors": 1, "_id": 0})     // actors
db.coll.find({"x": 1}, {"actors": 0, "summary": 0}) // all but "actors" and "summary"

// Sort, skip, limit
db.coll.find({}).sort({"year": 1, "rating": -1}).skip(10).limit(3)

// Read Concern
db.coll.find().readConcern("majority")


Update----------

db.coll.updateOne({"_id": 1}, {$set: {"year": 2016, name: "Max"}})
db.coll.updateOne({"_id": 1}, {$unset: {"year": 1}})
db.coll.updateOne({"_id": 1}, {$rename: {"year": "date"} })
db.coll.updateOne({"_id": 1}, {$inc: {"year": 5}})
db.coll.updateOne({"_id": 1}, {$mul: {price: NumberDecimal("1.25"), qty: 2}})
db.coll.updateOne({"_id": 1}, {$min: {"imdb": 5}})
db.coll.updateOne({"_id": 1}, {$max: {"imdb": 8}})
db.coll.updateOne({"_id": 1}, {$currentDate: {"lastModified": true}})
db.coll.updateOne({"_id": 1}, {$currentDate: {"lastModified": {$type: "timestamp"}}})

// Array
db.coll.updateOne({"_id": 1}, {$push :{"array": 1}})
db.coll.updateOne({"_id": 1}, {$pull :{"array": 1}})
db.coll.updateOne({"_id": 1}, {$addToSet :{"array": 2}})
db.coll.updateOne({"_id": 1}, {$pop: {"array": 1}})  // last element
db.coll.updateOne({"_id": 1}, {$pop: {"array": -1}}) // first element
db.coll.updateOne({"_id": 1}, {$pullAll: {"array" :[3, 4, 5]}})
db.coll.updateOne({"_id": 1}, {$push: {"scores": {$each: [90, 92]}}})
db.coll.updateOne({"_id": 2}, {$push: {"scores": {$each: [40, 60], $sort: 1}}}) // array sorted
db.coll.updateOne({"_id": 1, "grades": 80}, {$set: {"grades.$": 82}})
db.coll.updateMany({}, {$inc: {"grades.$[]": 10}})
db.coll.updateMany({}, {$set: {"grades.$[element]": 100}}, {multi: true, arrayFilters: [{"element": {$gte: 100}}]})

// FindOneAndUpdate
db.coll.findOneAndUpdate({"name": "Max"}, {$inc: {"points": 5}}, {returnNewDocument: true})

// Upsert
db.coll.updateOne({"_id": 1}, {$set: {item: "apple"}, $setOnInsert: {defaultQty: 100}}, {upsert: true})

// Replace
db.coll.replaceOne({"name": "Max"}, {"firstname": "Maxime", "surname": "Beugnet"})

// Write concern
db.coll.updateMany({}, {$set: {"x": 1}}, {"writeConcern": {"w": "majority", "wtimeout": 5000}})


Delete--------

db.coll.deleteOne({name: "Max"})
db.coll.deleteMany({name: "Max"}, {"writeConcern": {"w": "majority", "wtimeout": 5000}})
db.coll.deleteMany({}) // WARNING! Deletes all the docs but not the collection itself and its index definitions
db.coll.findOneAndDelete({"name": "Max"})









TRANSACTINOS--------------------------------------------------------------------------


-- Task 01: Create table persons
CREATE TABLE persons (
    personID INT PRIMARY KEY,
    FirstName VARCHAR(50),
    LastName VARCHAR(50),
    Address VARCHAR(100),
    City VARCHAR(50),
    Age INT
);

-- Insert 10 records
INSERT INTO persons VALUES
(1, 'John', 'Doe', '123 Main St', 'City1', 25),
(2, 'Jane', 'Smith', '456 Oak St', 'City2', 30),
-- ... (Insert 8 more records)

-- Create a savepoint after inserting five records
SAVEPOINT savepoint1;

-- Update the record on personID=7 where FirstName='Erum'
UPDATE persons SET FirstName = 'Rida' WHERE personID = 7;

-- Rollback to savepoint1
ROLLBACK TO SAVEPOINT savepoint1;


PSQL-----------------------------------------------------

1---- compute bonus amount 
ACCEPT employee_number CHAR PROMPT 'Enter Employee Number: '

DECLARE
   v_salary employees.salary%TYPE;
   v_bonus  NUMBER;
BEGIN
   SELECT salary INTO v_salary FROM employees WHERE employee_id = &employee_number;

   IF v_salary < 1000 THEN
      v_bonus := 0.1 * v_salary;
   ELSIF v_salary BETWEEN 1000 AND 1500 THEN
      v_bonus := 0.15 * v_salary;
   ELSE
      v_bonus := 0.2 * v_salary;
   END IF;

   DBMS_OUTPUT.PUT_LINE('Bonus Amount: ' || v_bonus);
END;
/


2------ update salary if comission is null
ACCEPT employee_id CHAR PROMPT 'Enter Employee ID: '

DECLARE
   v_commission employees.commission_pct%TYPE;
BEGIN
   SELECT commission_pct INTO v_commission FROM employees WHERE employee_id = &employee_id;

   IF v_commission IS NULL THEN
      UPDATE employees SET salary = salary + NVL(commission, 0) WHERE employee_id = &employee_id;
   END IF;
END;
/


3------ get dept name for dept num 30
DECLARE
   v_department_name departments.department_name%TYPE;
BEGIN
   SELECT department_name INTO v_department_name FROM departments WHERE department_id = 30;
   DBMS_OUTPUT.PUT_LINE('Department Name: ' || v_department_name);
END;
/


4------Find Salary for Employee in DeptNo 20:
DECLARE
   v_salary employees.salary%TYPE;
BEGIN
   SELECT salary INTO v_salary FROM employees WHERE department_id = 20;
   DBMS_OUTPUT.PUT_LINE('Salary: ' || v_salary);
END;
/


5------Update Salary with 10% Increase:
ACCEPT emp_number CHAR PROMPT 'Enter Employee Number: '

DECLARE
   v_employee_number employees.employee_id%TYPE := &emp_number;
BEGIN
   UPDATE employees SET salary = salary * 1.1 WHERE employee_id = v_employee_number;
   COMMIT;
END;
/


6-------Add Amount for Employees in Dept with Salary > 5000:
ACCEPT dept_number CHAR PROMPT 'Enter Department Number: '

CREATE OR REPLACE PROCEDURE AddAmountForDept(
   p_dept_number IN departments.department_id%TYPE
) AS
BEGIN
   UPDATE employees SET salary = salary + 1000
   WHERE department_id = p_dept_number AND salary > 5000;
   COMMIT;
END;
/



-- 7a. Create view to display each designation and number of employees with that designation.
CREATE VIEW EmpCountByDesignation AS
SELECT job_id, COUNT(*) AS employee_count
FROM employees
GROUP BY job_id;

-- 7b. Create view to display details of all employees except king.
CREATE VIEW EmployeesExceptKing AS
SELECT empno, ename, deptno, dname
FROM employees e
JOIN departments d ON e.deptno = d.deptno
WHERE ename != 'KING';

-- 7c. Create view to display details of all employees.
CREATE VIEW AllEmployees AS
SELECT empno, ename, deptno, dname
FROM employees e
JOIN departments d ON e.deptno = d.deptno;

-- 8. Add two numbers and display the sum.
ACCEPT num1 CHAR PROMPT 'Enter Number 1: '
ACCEPT num2 CHAR PROMPT 'Enter Number 2: '

DECLARE
   v_num1 NUMBER := &num1;
   v_num2 NUMBER := &num2;
   v_sum NUMBER;
BEGIN
   v_sum := v_num1 + v_num2;
   DBMS_OUTPUT.PUT_LINE('Sum: ' || v_sum);
END;
/

-- 9. Print sum of numbers between given boundaries.
ACCEPT lower_boundary CHAR PROMPT 'Enter Lower Boundary: '
ACCEPT upper_boundary CHAR PROMPT 'Enter Upper Boundary: '

DECLARE
   v_lower NUMBER := &lower_boundary;
   v_upper NUMBER := &upper_boundary;
   v_sum   NUMBER := 0;
BEGIN
   FOR i IN v_lower..v_upper LOOP
      v_sum := v_sum + i;
   END LOOP;
   DBMS_OUTPUT.PUT_LINE('Sum: ' || v_sum);
END;
/

-- 10. Retrieve employee details by employee number.
ACCEPT emp_number CHAR PROMPT 'Enter Employee Number: '

DECLARE
   v_emp_number NUMBER := &emp_number;
   v_emp_name   employees.ename%TYPE;
   v_hire_date  employees.hiredate%TYPE;
   v_dept_name  departments.dname%TYPE;
BEGIN
   SELECT ename, hiredate, dname
   INTO v_emp_name, v_hire_date, v_dept_name
   FROM employees e
   JOIN departments d ON e.deptno = d.deptno
   WHERE empno = v_emp_number;
   DBMS_OUTPUT.PUT_LINE('Employee Name: ' || v_emp_name);
   DBMS_OUTPUT.PUT_LINE('Hire Date: ' || v_hire_date);
   DBMS_OUTPUT.PUT_LINE('Department Name: ' || v_dept_name);
END;
/

-- 11. Insert data into Employee and Department tables.
ACCEPT emp_name CHAR PROMPT 'Enter Employee Name: '
ACCEPT emp_salary CHAR PROMPT 'Enter Employee Salary: '
ACCEPT emp_dept CHAR PROMPT 'Enter Employee Department: '
ACCEPT dept_name CHAR PROMPT 'Enter Department Name: '

DECLARE
   v_emp_name     employees.ename%TYPE := '&emp_name';
   v_emp_salary   employees.salary%TYPE := '&emp_salary';
   v_emp_dept     employees.deptno%TYPE := '&emp_dept';
   v_dept_name    departments.dname%TYPE := '&dept_name';
   v_emp_id       NUMBER;
   v_dept_id      NUMBER;
BEGIN
   INSERT INTO employees(ename, salary, deptno) VALUES (v_emp_name, v_emp_salary, v_emp_dept)
   RETURNING employee_id INTO v_emp_id;

   INSERT INTO departments(department_name) VALUES (v_dept_name)
   RETURNING department_id INTO v_dept_id;

   COMMIT;

   DBMS_OUTPUT.PUT_LINE('Employee ID: ' || v_emp_id);
   DBMS_OUTPUT.PUT_LINE('Department ID: ' || v_dept_id);
END;
/

-- 12. Find the first employee with salary > $2500 and higher in command than employee 7499.
DECLARE
   v_employee_name employees.ename%TYPE;
BEGIN
   FOR emp_rec IN (SELECT ename FROM employees WHERE salary > 2500 AND employee_id > 7499 ORDER BY employee_id)
   LOOP
      v_employee_name := emp_rec.ename;
      EXIT; -- Exit after finding the first employee
   END LOOP;

   DBMS_OUTPUT.PUT_LINE('First Employee with Salary > $2500 and Higher in Command: ' || v_employee_name);
END;
/

-- 13. Print sum of first 100 numbers.
DECLARE
   v_sum NUMBER := 0;
BEGIN
   FOR i IN 1..100 LOOP
      v_sum := v_sum + i;
   END LOOP;
   DBMS_OUTPUT.PUT_LINE('Sum of First 100 Numbers: ' || v_sum);
END;
/






Cursor------------------------------------------

-- Task 1: Using Cursors, Print a List of Managers and the Name of Departments.
DECLARE
   CURSOR manager_cursor IS
      SELECT e.ename AS employee_name, d.dname AS department_name
      FROM employees e
      JOIN departments d ON e.deptno = d.deptno
      WHERE e.job = 'MANAGER';

   v_employee_name employees.ename%TYPE;
   v_department_name departments.dname%TYPE;
BEGIN
   OPEN manager_cursor;
   LOOP
      FETCH manager_cursor INTO v_employee_name, v_department_name;
      EXIT WHEN manager_cursor%NOTFOUND;

      DBMS_OUTPUT.PUT_LINE('Manager: ' || v_employee_name || ', Department: ' || v_department_name);
   END LOOP;
   CLOSE manager_cursor;
END;
/

-- Task 2: Using Cursors, Retrieve Records of Employees with Salary > 5000.
DECLARE
   CURSOR high_salary_cursor IS
      SELECT * FROM employees WHERE salary > 5000;

   v_employee_record employees%ROWTYPE;
BEGIN
   OPEN high_salary_cursor;
   LOOP
      FETCH high_salary_cursor INTO v_employee_record;
      EXIT WHEN high_salary_cursor%NOTFOUND;

      DBMS_OUTPUT.PUT_LINE('Employee ID: ' || v_employee_record.employee_id ||
                           ', Employee Name: ' || v_employee_record.ename ||
                           ', Salary: ' || v_employee_record.salary);
   END LOOP;
   CLOSE high_salary_cursor;
END;
/

-- Task 3: Update Salary of Employee with 10% Increase (Pass empno as Argument).
DECLARE
   PROCEDURE UpdateSalary(p_empno IN NUMBER) IS
   BEGIN
      UPDATE employees SET salary = salary * 1.1 WHERE employee_id = p_empno;
      COMMIT;
   END;

   v_empno NUMBER := &emp_number;
BEGIN
   UpdateSalary(v_empno);
   DBMS_OUTPUT.PUT_LINE('Salary Updated for Employee with empno ' || v_empno);
END;
/

-- Task 4: Procedure to Add Amount for Employees with Salary > 5000 and deptno Argument.
CREATE OR REPLACE PROCEDURE AddAmountForDept(
   p_dept_number IN departments.department_id%TYPE
) AS
BEGIN
   UPDATE employees SET salary = salary + 1000
   WHERE department_id = p_dept_number AND salary > 5000;
   COMMIT;
END;
/

-- Example of Calling Procedure from Task 4 with deptno Argument.
-- Execute the following line after running the script:
-- EXEC AddAmountForDept(10);




Triggers-------------------------------------------------
-- Trigger to update PreviousName when Name changes
CREATE OR REPLACE TRIGGER tr_update_previous_name
BEFORE UPDATE ON Person
FOR EACH ROW
BEGIN
  IF :OLD.Name IS NOT NULL AND :NEW.Name IS NOT NULL AND :OLD.Name != :NEW.Name THEN
    :NEW.PreviousName := :OLD.Name;
  END IF;
END;
/

-- Trigger to update SameNameCount on insert
CREATE OR REPLACE TRIGGER tr_update_same_name_count_insert
BEFORE INSERT ON Person
FOR EACH ROW
BEGIN
  :NEW.SameNameCount := (
    SELECT COUNT(*)
    FROM Person
    WHERE Name = :NEW.Name
  ) + 1;
END;
/

-- Trigger to update SameNameCount on update
CREATE OR REPLACE TRIGGER tr_update_same_name_count_update
BEFORE UPDATE ON Person
FOR EACH ROW
BEGIN
  IF :OLD.Name IS NOT NULL AND :NEW.Name IS NOT NULL AND :OLD.Name != :NEW.Name THEN
    :NEW.SameNameCount := (
      SELECT COUNT(*)
      FROM Person
      WHERE Name = :NEW.Name
    ) + 1;
  END IF;
END;
/

-- Trigger to update SameNameCount on delete
CREATE OR REPLACE TRIGGER tr_update_same_name_count_delete
BEFORE DELETE ON Person
FOR EACH ROW
BEGIN
  :OLD.SameNameCount := :OLD.SameNameCount - 1;
END;
/




--------------------------------------------------------------------------------------------
joins-------
Write a SQL query to find the first name, last name, department name, city,
and state province for each employee.
2. Write a query to list the department name, where at least two employees are
working.
3. Fetch all the records where salary of employee is less than average salary for
all the departments.
4. Write a query to list the name, job name, annual salary, department id,
department name and city who earn 60000 in a year.
5. Write a query to find the first and last name of the employees who are also
Managers.
6. Retrieve employees who work in departments with IDs 10, 20, or 30, or who
earn more than $70,000
7. Display employee name, salary, department name where all employees have
matching department as well as employee does not have any departments.
8. Write a query in SQL to display the name of the department, average salary
and number of employees working in that department who got commission.
9. Write a query in SQL to display those employees who contain a letter z to their
first name and also display their last name, department, city, and state
province.
10.Retrieve the job IDs that exist in both the jobs and job_history tables.



--Task 1
select employees.first_name, employees.last_name, departments.department_name, locations.city, locations.state_province from
    ((employees inner join departments on employees.department_id = departments.department_id) 
    inner join locations on departments.location_id = locations.location_id);
    
--Task 2
select Count(employees.employee_id) as employee_count, departments.department_name from employees inner join departments
    on employees.department_id = departments.DEPARTMENT_ID group by departments.DEPARTMENT_NAME having count(employee_id) >= 2;
    
--Task 3
select * from employees where salary < (select avg(salary) from employees where department_id is not null) and department_id is not null;

--Task 4
select employees.first_name || ' ' || employees.last_name as full_name, jobs.job_title, (employees.salary * 12) as annual_salary,
    departments.department_id, departments.department_name, locations.city from 
    (((employees inner join departments on employees.department_id = departments.department_id)  
    inner join jobs on employees.job_id = jobs.job_id) 
    inner join locations on departments.location_id = locations.location_id) where (employees.salary * 12) > 60000;
    
--Task 5
select first_name, last_name from employees where employee_id = any (select distinct manager_id from employees);

--Task 6
select * from employees where department_id in (10, 20, 30) or (salary * 12) > 70000;

--Task 7
select employees.first_name || ' ' || employees.last_name as full_name, departments.department_name from employees 
    left join departments on employees.department_id = departments.department_id;
    
--Task 8
select departments.department_name, avg(employees.salary) as avg_salary, count(employees.employee_id) as num_of_employees
    from employees inner join departments on employees.department_id = departments.department_id 
    where employees.COMMISSION_PCT is not null 
    group by departments.DEPARTMENT_NAME;

--Task 9
select employees.first_name, employees.last_name, departments.department_name, locations.city, locations.state_province 
    from ((employees inner join departments on employees.department_id = departments.department_id)
    inner join locations on departments.location_id = locations.location_id) where employees.first_name like '%z%';
    
--Task 10
select jobs.job_id from jobs intersect select job_id from job_history; 






-------dml

1. Create a new user using SQL command Line and grant privileges. We are using
this user to create our own database with related tables.
2. Create table Jobs and job_History (ignore foreign keys relations) same fields as
given in HR Schema in which job_ID is considered as primary key in jobs table.
3. Change the data type of ‘job_ID’ from character to numeric in Jobs table.
4. Write a SQL statement to add job_id column in job_history table as foreign key
referencing to the primary key job_id of jobs table.
5. Insert a new row in jobs table having all the attributes and the job_ID.
6. Add Column Job_Nature in Jobs table.
7. Create table employee table as in hr database.
8. Write an SQL statement to add employee_id column in job_history table as
foreign key referencing to the primary key employee_id of employees table.
9. Drop column Job_Nature.
10. ALTER table EMPLOYEE created in question 5 and apply the constraint CHECK on
First_Name attribute such that ENAME should always be inserted in capital letters.
11. ALTER table EMPLOYEE created in question 7 and apply the constraint on Salary
attribute such that no two salaries of the employees should be similar.(Hint
Unique)
12. ALTER table Employee created in question 7 and apply constraint on Phone_No
such that Phone_No should not be entered empty (Hint modify).
13. Write a SQL statement to insert one row into the table employees.
14. Write a SQL statement to add a primary key for a combination of columns
employee_id and job_id in employees table.
15. Write a SQL statement to add an index named indx_job_id on job_id column in
the table job_history.
16. Write a SQL statement to remove all the data in employee.



create table jobs (job_id VARCHAR2(10 BYTE) primary key, job_title VARCHAR2(35 BYTE), min_salary NUMBER(6,0), max_salary NUMBER(6,0));
create table job_history (employee_id NUMBER(6,0), start_date DATE, end_date date, job_id VARCHAR2(10 BYTE), department_id NUMBER(4,0));

alter table jobs modify job_id NUMBER(6,0);

alter table job_history modify job_id Number(6, 0);
alter table job_history add FOREIGN key (job_id) REFERENCES jobs(job_id);

insert into jobs values (1, 'Developer', 35000, 125000);

alter table jobs add job_nature varchar2(35 byte);

create table employees (employee_id Number(6, 0) primary key, first_name varchar2(20 byte), last_name varchar2(25 byte) not null, email varchar2(25 byte) not null,
    phone_number varchar2(20 byte), hire_date date not null, job_id Number(6, 0) REFERENCES jobs(job_id) not null, salary number(8, 2),
    commission_pct number(2,2), manager_id number(6,0), department_id number(4,0));
    
alter table job_history add FOREIGN key (employee_id) REFERENCES employees(employee_id);

alter table jobs drop column job_nature;

alter table employees modify first_name check(first_name = Upper(first_name));

alter table employees modify salary UNIQUE;

alter table employees modify phone_number not null;

insert into employees (employee_id, first_name, last_name, email, phone_number, hire_date, job_id, salary, commission_pct, manager_id, department_id) 
    values (1, 'HUSSAIN', 'Mustafa', 'hussain@gmail.com', '0335873299', TO_DATE('2022-12-09','YYYY-MM-DD'), 1, 40000, 0.2, 1, 1);

alter table job_history drop COLUMN employee_id;
alter table job_history add employee_id number(6,0);
alter table employees drop primary key;
ALTER TABLE employees ADD CONSTRAINT PK_composite PRIMARY KEY (employee_id, job_id);

create index idx_job_id on job_history(job_id);

truncate table employees;




------grupbyhaving

1. For each department, retrieve the department no, the number of employees in
the department and their average salary.
2. Write a Query to display the number of employees with the same job.
3. Write a SQL query to find those employees who receive a higher salary than
the employee with ID 163. Return first name, last name.
4. Write a SQL query to select those departments where maximum salary is at
least 15000.
5. Write a SQL query to find those employees whose salary matches the lowest
salary of any of the departments. Return first name, last name and
department ID.
6. Write a query to display the employee number, name (first name and last
name) and job title for all employees whose salary is smaller than any salary
of those employees whose job title is IT_PROG.
7. Write a SQL query to find those employees who do not work in the
departments where managers’ IDs are between 100 and 200.Return all the
fields of the employees.
8. Display the manager number and the salary of the lowest paid employee of
that manager. Exclude anyone whose manager is not known. Exclude any
groups where the minimum salary is 2000. Sort the output in descending
order of the salary.
9. Insert into employees_BKP as it should copy the record of the employee
whose start date is ’13-JAN-01’ from job_History table.
10.Update salary of employees by 20% increment having minimum salary of
6000.
11.Delete the record of employees from employees_BKP who are manager and
belongs to the department ‘Finance’.
12.For each department that has more than five employees, retrieve the
department number and the number of its employees who are making more
than $20,000.


Task 1
select department_id, Count(employee_id), cast(avg(salary) as decimal(10,1)) as avg_salary from employees group by department_id;

--Task 2
select job_id, count(employee_id) from employees group by job_id;

--Task 3
select first_name, last_name from employees where salary > (select salary from employees where employee_id = 163);

--Task 4
select department_id, max(salary) from EMPLOYEES group by department_id having max(salary) > 15000;

--Task 5
select first_name, last_name, department_id from employees where salary = any (select min(salary) from employees group by department_id);

--Task 6
select job_id, first_name || ' ' || last_name AS full_name from employees where salary < all (select salary from employees where job_id = 'IT_PROG');

--Task 7
select * from employees where manager_id != all (select manager_id from employees where manager_id between 100 and 200);

--Task 8
select manager_id, min(salary) as salary from employees where salary = any (select min(salary) from employees group by manager_id)
    and salary > 2000 and manager_id is not null group by manager_id order by salary desc;
 
--Task 9  
create table employees_bkp as (select * from employees);
insert into EMPLOYEES_BKP (select * from EMPLOYEES where employee_id = (select employee_id from JOB_HISTORY where start_date='13-JAN-01'));

--Task 10
update employees set salary = salary + (salary * 0.2) where salary <= 6000;

--Task 11
delete from EMPLOYEES_BKP where employee_id = any (select DISTINCT manager_id from employees) and DEPARTMENT_ID = (select department_id from DEPARTMENTS where department_name = 'Finance');

--Task 12
select department_id, count(employee_id) as employee_count from employees where salary >= 20000 group by DEPARTMENT_ID having count(employee_id) > 5;

