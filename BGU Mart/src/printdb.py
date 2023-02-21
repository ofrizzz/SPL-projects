from persistence import *


def main():
    print("Activities")
    lst = repo.activities.find_all()
    lst.sort(key=lambda x: x.date)
    for activity in lst:
        print(activity)

    print("Branches")
    lst = repo.branches.find_all()
    lst.sort(key=lambda x: x.id)
    for branch in lst:
        print(branch)

    print("Employees")
    lst = repo.employees.find_all()
    lst.sort(key=lambda x: x.id)
    for employee in lst:
        print(employee)

    print("Products")
    lst = repo.products.find_all()
    lst.sort(key=lambda x: x.id)
    for product in lst:
        print(product)

    print("Suppliers")
    lst = repo.suppliers.find_all()
    lst.sort(key=lambda x: x.id)
    for supplier in lst:
        print(supplier)

    print("\nEmployees report")
    lst = repo.employees.find_all()
    lst.sort(key=lambda x: x.name)
    for employee in lst:
        employee.report()

    print("\nActivities report")
    lst = repo.activities.find_all()
    lst.sort(key=lambda x: x.date)
    for activitie in lst:
        activitie.report()


if __name__ == '__main__':
    main()
