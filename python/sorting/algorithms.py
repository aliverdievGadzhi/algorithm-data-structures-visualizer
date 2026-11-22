"""Sorting algorithms that record every compare and move as a step.

Each function sorts a copy of the input in ascending order and returns the response;
the final array is the `state` of the last step, which is always `done`.
"""

from .steps import record, start


def bubble_sort(values):
    a = list(values)
    out = start("bubble_sort", a)
    for end in range(len(a) - 1, 0, -1):
        swapped = False
        for j in range(end):
            bigger = a[j] > a[j + 1]
            record(out, "compare", [j, j + 1], f"{a[j]} {'>' if bigger else '<='} {a[j + 1]}", a)
            if bigger:
                a[j], a[j + 1] = a[j + 1], a[j]
                record(out, "swap", [j, j + 1], f"Swap {a[j + 1]} and {a[j]}", a)
                swapped = True
        if not swapped:
            break
    record(out, "done", None, "The array is sorted", a)
    return out


def insertion_sort(values):
    a = list(values)
    out = start("insertion_sort", a)
    for i in range(1, len(a)):
        key = a[i]
        j = i - 1
        while j >= 0:
            bigger = a[j] > key
            record(out, "compare", j, f"{a[j]} {'>' if bigger else '<='} {key} (the key)"
                   + (", shift it right" if bigger else ", stop"), a)
            if not bigger:
                break
            a[j + 1] = a[j]
            record(out, "overwrite", j + 1, f"Shift {a[j]} to index {j + 1}", a)
            j -= 1
        if j + 1 != i:
            a[j + 1] = key
            record(out, "insert", j + 1, f"Insert {key} at index {j + 1}", a)
    record(out, "done", None, "The array is sorted", a)
    return out


def quick_sort(values):
    """Lomuto partition with the last element as pivot."""
    a = list(values)
    out = start("quick_sort", a)

    def partition(lo, hi):
        pivot = a[hi]
        record(out, "pivot", hi, f"Pivot {pivot} at index {hi}, partition indexes {lo}..{hi}", a)
        i = lo
        for j in range(lo, hi):
            smaller = a[j] < pivot
            record(out, "compare", [j, hi], f"{a[j]} {'<' if smaller else '>='} pivot {pivot}", a)
            if smaller:
                if i != j:
                    a[i], a[j] = a[j], a[i]
                    record(out, "swap", [i, j], f"Move {a[i]} to the left part", a)
                i += 1
        if i != hi:
            a[i], a[hi] = a[hi], a[i]
            record(out, "swap", [i, hi], f"Put pivot {pivot} at its final index {i}", a)
        return i

    def sort(lo, hi):
        if lo < hi:
            p = partition(lo, hi)
            sort(lo, p - 1)
            sort(p + 1, hi)

    # shortcut: recursion goes n deep on sorted input, fine for visualizer-sized arrays
    # (a full snapshot per step already rules out thousands of elements).
    sort(0, len(a) - 1)
    record(out, "done", None, "The array is sorted", a)
    return out


def merge_sort(values):
    """Top-down merge sort; each merge writes the merged run back over the array."""
    a = list(values)
    out = start("merge_sort", a)

    def sort(lo, hi):
        if hi - lo < 2:
            return
        mid = (lo + hi) // 2
        sort(lo, mid)
        sort(mid, hi)
        left, right = a[lo:mid], a[mid:hi]
        record(out, "merge", [lo, hi - 1], f"Merge {left} and {right}", a)
        i = j = 0
        for k in range(lo, hi):
            if j == len(right) or (i < len(left) and left[i] <= right[j]):
                reason = f"{left[i]} <= {right[j]}" if j < len(right) else "right half is used up"
                a[k] = left[i]
                i += 1
                side = "left"
            else:
                reason = f"{right[j]} < {left[i]}" if i < len(left) else "left half is used up"
                a[k] = right[j]
                j += 1
                side = "right"
            record(out, "overwrite", k, f"Write {a[k]} from the {side} half to index {k} ({reason})", a)

    sort(0, len(a))
    record(out, "done", None, "The array is sorted", a)
    return out
