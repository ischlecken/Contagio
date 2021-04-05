import UIKit
import CoreData

func createPicture(
    image: UIImage,
    context: NSManagedObjectContext) -> Picture {
    let result = Picture(context:context)
    
    result.id = UUID().uuidString
    result.createts = Date()
    result.format = "image/png"
    result.data = image.pngData()
    
    return result
}
