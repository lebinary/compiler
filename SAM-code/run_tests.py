#!/usr/bin/python3

from typing import Dict
import pathlib
import re
import subprocess
import sys


def run_tests(operation: str, test_dir: str) -> Dict[str, bool]:
    """
    Execute all test cases in a given directory.  Returns a mapping of test cases to
    boolean value indicated whether each test case was passed.
    """
    test_path = pathlib.Path(test_dir)
    str_operation_tests = list(test_path.glob("test*"))
    str_operation_test_results = {}
    print(f"\nRunning tests in {test_dir}:")
    test_number_re = re.compile(r"test_(\d+)")
    for str_operation_test in sorted(str_operation_tests, key=lambda t: int(test_number_re.search(str(t)).groups()[0])):
        build_test_program(operation, str(str_operation_test))

        test_name = str(str_operation_test).replace(test_dir, "")

        print(f"\t{test_name}:  \t", end="")
        str_operation_test_results[test_name] = run_test_program()
        print(f"{'PASSED' if str_operation_test_results[test_name] else 'FAILED'}")

    return str_operation_test_results


def run_test_program() -> bool:
    """
    Executes the current contents of test.sam on the Sam virtual machine.

    Returns True if Sam exits with status 1, else returns False.
    """
    try:
        test_result = subprocess.run([
            "java", "-Dfile.encoding=UTF8", "-cp", "SaM-2.6.3.jar", "edu.utexas.cs.sam.ui.SamText", "test.sam"
        ], stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=5)
    except subprocess.TimeoutExpired:
        print("test.sam timed out during execution.  Check for infinite loops")
        exit(1)

    if len(test_result.stderr) > 0:
        # Exit so that bad input code isn't overwritten and students can investigate
        print(f"SaM encountered an error in test.sam: \n\t{test_result.stderr.decode('utf-8')}")
        exit(1)

    output = test_result.stdout.decode('utf-8')
    exit_status_true_re = re.compile("Exit Status: 1\\b")
    return exit_status_true_re.search(output) is not None


def build_test_program(operation: str, test_input: str) -> None:
    """
    Concatenate matching test_input, operation.sam, and test_output files
    and write result to test.sam.
    """
    with open("test.sam", 'w', encoding='utf-8') as test_file:
        test_file.write(f"// {test_input}\n")

        with open(test_input, encoding='utf-8') as test_template_file:
            test_template = "".join(test_template_file.readlines())
            with open(f"operations/{operation}.sam", encoding='utf-8') as operation_implementation:
                operation_code = "".join(operation_implementation.readlines())
                test_file_code = test_template.replace(f"// {operation}", operation_code)
                test_file.write(test_file_code)


if __name__ == '__main__':
    if len(sys.argv) > 1:
        operation = sys.argv[1]
        str_operation_test = sys.argv[2]
        str_operation_test_results = {}

        test_dir = f"test_cases/{operation}"
        test_name = str_operation_test.replace(test_dir, "")

        build_test_program(operation, test_dir + "/" + str_operation_test)
        print(f"\t{test_name}:  \t", end="", flush=True)
        str_operation_test_results[test_name] = run_test_program()
        print(f"{'PASSED' if str_operation_test_results[test_name] else 'FAILED'}")
    else:
        for op in [
            "str_len",
            "str_concat",
            "str_repeat",
            "str_rev",
            "str_cmp"
        ]:
            test_dir = pathlib.Path(f"test_cases/{op}/")
            test_results = run_tests(op, str(test_dir))
            case_count = len(test_results)
            passed_count = sum(v for v in test_results.values() if v)
            print(f"{op} Score: {passed_count} / {case_count}")
