#!/bin/bash

java -cp ../build/canopy.jar:build/canopy-classes.jar:../../jpf-core/build/jpf.jar:../../jpf-symbc/build/jpf-symbc-classes.jar:../../jpf-symbc/build/jpf-symbc.jar:../../jpf-symbc/lib/com.microsoft.z3.jar:../lib/* edu.cmu.sv.isstac.canopy.batch.BatchProcessorIncreasingSize $@
