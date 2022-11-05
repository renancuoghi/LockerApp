
# Locker App

This project is a entity locker implementation. The goal of this projets is create a class to work as a row-level database lock.

For lock entities was used ReentrantLock with a ConcurrentHashMap, when a entity is locked (by id). For each entity the entity locker will create a ReentrantLock to control locked entities.

For global lock, was create a global ReentrantLock when this lock is adquired all new entities will get the same global lock, so each new entity will be locked until unlock everything.

In case of escalation, was created a class EscalationEntityLocker, this class is a inheritance from EntityLocker, the diferente is EscalationEntityLocker have a escaltion detecter, this away the developer can control how many entities each thread can lead without lock everything.





## Project information

#### If separate the soluction with two types of classes


| Class   | Description                           |
| :---------- | :---------------------------------- |
| `EntityLocker` | Main implementation of entity locker |
| `EscalationEntityLocker` | Implementation using a escaltion detector |

#### Test

I did all the tests using junit, you can find them in test path with the principal tests, you may use maven to run all tests.


## How to run

To run this project you may run with:

## Maven

```bash
  mvn clean install
  mvn test
```

## Docker
```bash
  docker-compose up --build
```


## Licença

[MIT](https://choosealicense.com/licenses/mit/)

