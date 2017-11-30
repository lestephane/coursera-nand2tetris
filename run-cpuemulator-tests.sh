#!/bin/bash
set -euo pipefail

if [ -f "vmtranslator/out/production/classes/VMTranslator.class" ]; then
    vmclasspath="vmtranslator/out/production/classes"
else
    vmclasspath="vmtranslator/build/classes/java/main"
fi

runtest() {
    local input="${1}"
    if [ -d "${test}" ]; then
        testoutput="${input}/$(basename "${test}")"
    else
        testoutput="${input/.vm/}"
    fi
    rm -f "${testoutput}.asm" "${testoutput}.out"
    java -cp "${vmclasspath}" VMTranslator "${input}"
    tools/CPUEmulator.sh "${testoutput}.tst" || {
        diff -w "${testoutput}.out" "${testoutput}.cmp"
        return 1
    }
}

testlist() {
cat <<EOF
07/StackArithmetic/SimpleAdd/SimpleAdd.vm
07/StackArithmetic/StackTest/StackTest.vm
07/MemoryAccess/BasicTest/BasicTest.vm
07/MemoryAccess/PointerTest/PointerTest.vm
07/MemoryAccess/StaticTest/StaticTest.vm
08/ProgramFlow/BasicLoop/BasicLoop.vm
08/ProgramFlow/FibonacciSeries/FibonacciSeries.vm
08/FunctionCalls/SimpleFunction/SimpleFunction.vm
08/FunctionCalls/NestedCall
EOF
}

# 08/FunctionCalls/FibonacciElement/FibonacciElement \
# 08/FunctionCalls/StaticsTest/StaticsTest \

errorcount=0
testsuite() {
    echo "1..$(testlist | wc -l)"
    testnumber=1
    for test in $(testlist); do
        testoutput=$(runtest "${test}" 2>&1) && {
            echo -n "ok "
        } || {
            echo -n "not ok "
            errorcount=$((errorcount + 1))
        }
        echo "$((testnumber++)) - ${test}"
        echo "${testoutput}" | while read line; do echo "# ${line}"; done
    done
    return $errorcount
}

testsuite
echo $?
