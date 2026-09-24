# Notes

El primer reto en este proyecto es decidir la estructura de datos para la memtable. Las estructuras de datos mencionadas en DDIA son:
- red-black tree
- skip list
- trie

Un red-black tree es un árbol binario auto-balanceado. Su búsqueda, inserción y eliminación son O(log n), y son adecuados para range scans. Uno de sus problemas es que los nodos pueden acabar dispersos por el heap, y la CPU haciendo pointer-chasing, tiene mala localidad. Otro de sus problemas es la concurrencia, una inserción puede provocar varias operaciones, como recolorización, rotación o modificación de padre o hijo, que afectan a varios nodos.

Una skip list es un tipo de estructura de datos que consigue aproximadamente la misma funcionalidad que un árbol balanceado, pero que funciona como una lista enlazada con diferentes niveles, que cada uno contiene menos elementos, consiguiendo una búsqueda aproximada O(log n), aunque no se garantiza. La principal ventaja de esta estructura de datos es la concurrencia. Como en el caso anterior, esta estructura de datos también tiene mala localidad.

Un trie es un tipo de dato completamente diferente, este no compara las claves enteras, se aprovecha de la estructura interna de las claves, navegando por cada caracter o byte de las claves, consiguiendo un coste O(L) siendo L longitud de la clave. Su punto fuerte son los prefijos, haciéndolos adecuados para queries de prefijos, autocompletado, rutado, etc. Su principal problema es la memoria.

Para este proyecto personal, el tipo de dato escogido es el skip list, 