app TargetClass.main(..) - .print(false)
disl: before
app TargetClass.print(..) - try:begin
app TargetClass.print(..) - try:end
disl: afterReturning
app TargetClass.main(..) - .print(true)
disl: before
app TargetClass.print(..) - try:begin
app TargetClass.print(..) - throw
disl: afterThrowing
app TargetClass.print(..) - catch
