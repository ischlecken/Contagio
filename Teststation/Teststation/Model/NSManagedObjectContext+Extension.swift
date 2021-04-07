import CoreData

extension NSManagedObjectContext {
    
    @discardableResult
    public func saveContext() -> Bool {
        print("saveContext()")
        
        guard hasChanges else { return false }
        
        do {
            print("  -->something changed...")
            
            try save()
        }  catch {
            // Replace this implementation with code to handle the error appropriately.
            // fatalError() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
            let nserror = error as NSError
            fatalError("Unresolved error \(nserror), \(nserror.userInfo)")
        }
        
        return true
    }
    
    public func forcedSave() {
        print("forcedSave()")
        
        do {
            try save()
        }  catch {
            let nserror = error as NSError
            fatalError("Unresolved error \(nserror), \(nserror.userInfo)")
        }
    }
}
