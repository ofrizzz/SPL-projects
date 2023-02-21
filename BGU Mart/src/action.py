from persistence import *
import sys

def apply(action : Activitie):
    products = repo.products.find(id=action.product_id)
    if(len(products) == 0):
        return
    product = products[0]

    if(action.quantity == 0):
        return
    
    if(action.quantity < 0 and product.quantity + action.quantity < 0):
        return

    product.quantity += action.quantity
    repo.products.update(product, quantity=product.quantity)    
    repo.activities.insert(action)

def main(args : list[str]):
    
    if(len(args) != 2):
        print("ERROR! Usage: python3 action.py <inputfile>")
        return
    inputfilename : str = args[1]

    with open(inputfilename) as inputfile:
        for line in inputfile:
            splittedline : list[str] = line.strip().split(", ")
            activitie = Activitie(splittedline[0], int(splittedline[1]), splittedline[2], splittedline[3])
            apply(activitie)
            


if __name__ == '__main__':
    main(sys.argv)