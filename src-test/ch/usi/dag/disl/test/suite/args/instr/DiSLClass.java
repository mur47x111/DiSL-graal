package ch.usi.dag.disl.test.suite.args.instr;

import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorContext;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorMode;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class DiSLClass {

	@AfterReturning(marker = BodyMarker.class, scope = "TargetClass.*")
	public static void postcondition(final MethodStaticContext sc, final ArgumentProcessorContext pc) {

		System.out.printf("disl: args for %s %s\n", sc.thisMethodFullName(), sc.thisMethodDescriptor());

		final Object[] args = pc.getArgs(ArgumentProcessorMode.METHOD_ARGS);
		LEVEL1: for(int i = 0 ; i < args.length; ++i) {
			System.out.printf("disl: \targ[%d]\n", i);


			if (args[i] instanceof Object[]) {
				final Object[] argsarr = (Object[])args[i];
				LEVEL2: for (final Object arg : argsarr) {

					if (arg instanceof Object[]) {
						final Object[] argsarr2 = (Object[])arg;
						LEVEL3: for (final Object arg2 : argsarr2) {
							final Object a = arg2;
							final String n = a.getClass().getCanonicalName();

							// code duplication as this is DiSL class
							// and we would have to move it out

							if(n.equals ("java.lang.Integer")) {
								System.out.printf("disl: \t\t\t\t%s\n", n);
								System.out.printf("disl: \t\t\t\t%s\n", a.toString());
								continue LEVEL3;
							}

							if(n.equals ("java.lang.Float")) {
								System.out.printf("disl: \t\t\t\t%s\n", n);
								System.out.printf("disl: \t\t\t\t%s\n", a.toString());
								continue LEVEL3;
							}

							if(n.equals ("java.lang.Double")) {
								System.out.printf("disl: \t\t\t\t%s\n", n);
								System.out.printf("disl: \t\t\t\t%s\n", a.toString());
								continue LEVEL3;
							}

							if(n.equals ("java.lang.String")) {
								System.out.printf("disl: \t\t\t\t%s\n", n);
								System.out.printf("disl: \t\t\t\t%s\n", a.toString());
								continue LEVEL3;
							}

							// default
							System.out.printf("disl: \t\t\t\t%s\n", n);
							System.out.printf("disl: \t\t\t\t== cannot print ==\n");
						}

					} else {
						final Object a = arg;
						final String n = a.getClass().getCanonicalName();

						if(n.equals ("java.lang.Integer")) {
                            System.out.printf("disl: \t\t\t%s\n", n);
                            System.out.printf("disl: \t\t\t%s\n", a.toString());
                            continue LEVEL2;
                        }

                        if(n.equals ("java.lang.Float")) {
                            System.out.printf("disl: \t\t\t%s\n", n);
                            System.out.printf("disl: \t\t\t%s\n", a.toString());
                            continue LEVEL2;
                        }

                        if(n.equals ("java.lang.Double")) {
                            System.out.printf("disl: \t\t\t%s\n", n);
                            System.out.printf("disl: \t\t\t%s\n", a.toString());
                            continue LEVEL2;
                        }

                        if(n.equals ("java.lang.String")) {
                            System.out.printf("disl: \t\t\t%s\n", n);
                            System.out.printf("disl: \t\t\t%s\n", a.toString());
                            continue LEVEL2;
                        }

                        // default
                        System.out.printf("disl: \t\t\t%s\n", n);
                        System.out.printf("disl: \t\t\t== cannot print ==\n");
					}
				}

			} else {
				final Object a = args[i];
				final String n = a.getClass().getCanonicalName();

				if(n.equals ("java.lang.Integer")) {
                    System.out.printf("disl: \t\t%s\n", n);
                    System.out.printf("disl: \t\t%s\n", a.toString());
                    continue LEVEL1;
                }

                if(n.equals ("java.lang.Float")) {
                    System.out.printf("disl: \t\t%s\n", n);
                    System.out.printf("disl: \t\t%s\n", a.toString());
                    continue LEVEL1;
                }

                if(n.equals ("java.lang.Double")) {
                    System.out.printf("disl: \t\t%s\n", n);
                    System.out.printf("disl: \t\t%s\n", a.toString());
                    continue LEVEL1;
                }

                if(n.equals ("java.lang.String")) {
                    System.out.printf("disl: \t\t%s\n", n);
                    System.out.printf("disl: \t\t%s\n", a.toString());
                    continue LEVEL1;
                }

                // default
                System.out.printf("disl: \t\t%s\n", n);
                System.out.printf("disl: \t\t== cannot print ==\n");
			}
		}
	}
}
