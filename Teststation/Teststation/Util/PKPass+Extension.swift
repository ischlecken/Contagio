import PassKit

extension PKPass {
    
    static func loadPass(_ name:String) -> PKPass? {
        var result : PKPass? = nil
        
        if let fileURL = Bundle.main.url(forResource: name, withExtension: "pkpass") {
            if let data = NSData(contentsOf: fileURL) as Data? {
                do {
                    result = try PKPass(data: data)
                } catch {
                    print("Error saving managed object context: \(error)")
                }
            }
        }
        
        return result
    }
}
