# SaM String Library

## Implementation

Fill in the implementation for each string operation file in the `operations/` directory.  The API for each operation is described in a comment in the top of the file.  You may assume that the inputs for your code are arranged on the stack starting at offset 0, and you should leave the output value for your code at offset 0 when it completes.

## Running Tests

You can run all tests by

```
python3 run_tests.py
```

This requires Python3 to be installed locally and assumes your python executable is names `python3`.  Substitute the appropriate name if your system has a different one.

You can run a single test by

```
python3 run_tests.py <op_name> <test_name>
```

For example `python3 run_tests.py str_cmp test_1` will run your str_cmp code against the first test case.

If the SaM virtual machine encounters an error, the test runner will exit early and leave the code that caused the error in `test.sam`.  You can investigate the error by loading that file in the SaM virtual machine using the GUI.

You can start the SaM virtual machine GUI by running

```
java -Dfile.encoding=UTF8 -jar SaM-2.6.3.jar
```
