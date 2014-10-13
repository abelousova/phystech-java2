phystech-java2
==============

**Домашние задания по курсу Java 2**

**Задание 1.**

*Отчет:*

+ Нарисована схема модулей и классов
+ Выпилен весь мусор (корни старых проектов) + переработаны интерфейсы Table и др.
+ База данных разделена на модули:
 - database-main
 - table-api
 - table-impl
 - table-utils
 - shell-api
 - shell-impl
 - shell-commands
 - database-state

+ Используется Spring ApplicationContext, конфигурируемый через аннотации.
+ Команды для Shell теперь добавляются сами через @Autowired. ShellState тоже заполняется автоматически через @Autowired + путь к базе данных выставляется через внешний .properties файл (см. корень проекта)
+ Прикручен логгинг через slf4j + log4j. Логгирует в файл (указывается через -Dlog.path=...) вызовы команд shell.
+ Использовала Google Guava и Apache Commons.
+ Тесты проходятся.
