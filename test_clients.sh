#!/bin/bash

# Número inicial de clientes
num_clients=1

# Número máximo de clientes
max_clients=10

# Número grande para calcular el Fibonacci
large_number=20

# Directorio donde se encuentra el archivo JAR del cliente
client_dir="/home/swarch/jhonatan_andres/helloworld_2/helloworld-ret/client/build/libs"

# Nombre del archivo JAR del cliente
client_jar="client.jar"

echo "$large_number" > input.txt

# Función para ejecutar múltiples instancias del cliente al mismo tiempo
run_multiple_clients() {
    for ((i = 0; i < num_clients; i++)); do
        java -jar "$client_dir/$client_jar" < input.txt &
        wait
    done
    wait # Esperar a que todos los clientes terminen
}

# Bucle para aumentar gradualmente el número de clientes y ejecutar pruebas
while ((num_clients <= max_clients)); do
    echo "Número de clientes: $num_clients"
    run_multiple_clients
    ((num_clients++)) #Aumentar el número de clientes para la próxima iteración
done

rm input.txt