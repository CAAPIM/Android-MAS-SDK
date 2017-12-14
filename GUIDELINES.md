# CA Technologies Java Style Guide

This document describes the Java coding style of the Android Mobile SDK team. This guideline is recommended for compliance with all Java implementations of our products.

## Review Android Coding Guidelines

Beyond the guidelines defined in this document, we also recommend reviewing the Android official coding guidelines.

* [Code Style Rules](https://source.android.com/source/code-style.html)
* [Performance Tips](https://developer.android.com/training/articles/perf-tips.html)

### Android Mobile SDK

For more information about the Android Mobile SDK, see the [developer documentation](http://mas.ca.com).

## Table of Contents

* [Documentation](#documentation)
* [Naming Convention](#naming-convention)
  * [Class files](#class-files)
  * [Resource files](#resource-files)
* [Code Style](#code-style)
* [Coding Guidelines](#coding-guidelines)

## Documentation

All code **should be properly documented**. Code documentation should adhere to JavaDoc format.

## Naming Convention

### Class files
1. Class and modules names are written in [CamelCase](https://en.wikipedia.org/wiki/CamelCase).
2. Classes that extend Android components should end with the name of the component, e.g. MASLoginFragment, MASEnterpriseWebApplicationActivity.
3. Developer facing interfaces should start with **MAS**, e.g. MASUser, MASSecureLocalStorage.

### Resource files
1. Resource file names are written in lowercase with underscores, e.g. mas_main_layout.xml, otp_validation.xml, ca_logo.png.

## Code Style
1. Default Android Studio code style.

## Coding Guidelines
1. Prefer constants over enums, unless you need enum-specific features.
2. Use annotation @IntDef or @StringDef to represent a logical type and that its value should be one of the explicitly named constants.
3. All UI related features should be located within the MASUI module. Tag parameters or return values with appropriate annotations such as @LayoutRes and @MenuRes if methods return or expect these types.
4. Use **Parcelable** instead of **Serializable** when passing objects to activities or fragments.
5. Avoid unnecessary logging.
6. Include the class name as a tag for logging. e.g.
```java
private static final String TAG = MAS.class.getCanonicalName();
```
