import sqlite3
import atexit
from dbtools import Dao

# Data Transfer Objects:


class Employee(object):
    def __init__(self, id, name, salary, branche) -> None:
        self.id = id
        self.name = name
        self.salary = salary
        self.branche = branche

    def __str__(self) -> str:
        return "({}, '{}', {}, {})".format(self.id, self.name.decode(), float(self.salary), self.branche)

    def report(self):
        branche_name_query = "SELECT location FROM branches WHERE id = ?"
        tempname = repo._conn.cursor().execute(branche_name_query, (self.branche,)).fetchone()
        branche_name = repo._conn.cursor().execute(branche_name_query, (self.branche,)).fetchone()[0]

        activites_query = repo._conn.cursor().execute("SELECT * FROM activities WHERE quantity < 0 AND activator_id = ?", (self.id,)).fetchall()
        total_salary = 0
        for row in activites_query:
            product_id = row[0]
            quantity = row[1]
            price = repo._conn.cursor().execute("SELECT price FROM products WHERE id = ?", (product_id,)).fetchone()[0]
            total_salary -= float(price) * quantity

        print(self.name.decode() + " " + str(self.salary) + " " + branche_name.decode() + " " + str(total_salary)) 

class Supplier(object):
    def __init__(self, id, name, contact_information) -> None:
        self.id = id
        self.name = name
        self.contact_information = contact_information

    def __str__(self) -> str:
        return "({}, '{}', '{}')".format(self.id, self.name.decode(), self.contact_information.decode())


class Product(object):
    def __init__(self, id, description, price, quantity) -> None:
        self.id = id
        self.description = description
        self.price = price
        self.quantity = quantity

    def __str__(self) -> str:
        return "({}, '{}', {}, {})".format(self.id, self.description.decode(), float(self.price), self.quantity)


class Branche(object):
    def __init__(self, id, location, number_of_employees) -> None:
        self.id = id
        self.location = location
        self.number_of_employees = number_of_employees

    def __str__(self) -> str:
        return "({}, '{}', {})".format(self.id, self.location.decode(), self.number_of_employees)


class Activitie(object):
    def __init__(self, product_id, quantity, activator_id, date) -> None:
        self.product_id = product_id
        self.quantity = quantity
        self.activator_id = activator_id
        self.date = int(date)

    def __str__(self) -> str:
        return "({}, {}, {}, '{}')".format(self.product_id, self.quantity, self.activator_id, self.date)

    def report(self):
        product_name_query = "SELECT description FROM products WHERE id = ?"
        product_name = repo._conn.cursor().execute(product_name_query, (self.product_id,)).fetchone()[0]
        
        supplier_name = "None"
        employee_name = "None"

        if self.quantity > 0:
            supplier_name_query = "SELECT name FROM suppliers WHERE id = ?"
            supplier_name = repo._conn.cursor().execute(supplier_name_query, (self.activator_id,)).fetchone()[0]
            supplier_name = "'" + supplier_name.decode() + "'"
        else:
            employee_name_query = "SELECT name FROM employees WHERE id = ?"
            employee_name = repo._conn.cursor().execute(employee_name_query, (self.activator_id,)).fetchone()[0]
            employee_name = "'" + employee_name.decode() + "'"

        print("('" + str(self.date) + "', '" + product_name.decode() + "', " + str(self.quantity)  + ", " +  employee_name + ", " + supplier_name + ")")


# Repository
class Repository(object):
    def __init__(self):
        self._conn = sqlite3.connect('bgumart.db')
        self._conn.text_factory = bytes
        self.employees = Dao(Employee, self._conn)
        self.suppliers = Dao(Supplier, self._conn)
        self.branches = Dao(Branche, self._conn)
        self.products = Dao(Product, self._conn)
        self.activities = Dao(Activitie, self._conn)

    def _close(self):
        self._conn.commit()
        self._conn.close()

    def create_tables(self):
        self._conn.executescript("""
            CREATE TABLE employees (
                id              INT         PRIMARY KEY,
                name            TEXT        NOT NULL,
                salary          REAL        NOT NULL,
                branche    INT REFERENCES branches(id)
            );
    
            CREATE TABLE suppliers (
                id                   INTEGER    PRIMARY KEY,
                name                 TEXT       NOT NULL,
                contact_information  TEXT
            );

            CREATE TABLE products (
                id          INTEGER PRIMARY KEY,
                description TEXT    NOT NULL,
                price       REAL NOT NULL,
                quantity    INTEGER NOT NULL
            );

            CREATE TABLE branches (
                id                  INTEGER     PRIMARY KEY,
                location            TEXT        NOT NULL,
                number_of_employees INTEGER
            );
    
            CREATE TABLE activities (
                product_id      INTEGER REFERENCES products(id),
                quantity        INTEGER NOT NULL,
                activator_id    INTEGER NOT NULL,
                date            TEXT    NOT NULL
            );
        """)

    def execute_command(self, script: str) -> list:
        return self._conn.cursor().execute(script).fetchall()


# singleton
repo = Repository()

atexit.register(repo._close)
