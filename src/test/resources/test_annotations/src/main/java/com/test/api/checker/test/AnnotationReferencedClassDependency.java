package com.test.api.checker.tests;

import com.test.api.checker.tests.annotations.AnnotationWithClassReference;
import org.slf4j.Logger;

@AnnotationWithClassReference(Logger.class)
public class AnnotationReferencedClassDependency {

}
