from ipywidgets import widgets
from math import sqrt
from IPython.display import display
from ipywidgets import interact, interactive, fixed, interact_manual
import os,glob, dicom, cv2, random, csv
import matplotlib.pyplot as plt
import numpy as np # linear algebra
import pandas as pd # data processing, CSV file I/O (e.g. pd.read_csv) 
from skimage import measure, morphology, segmentation, filters
from skimage.segmentation import clear_border, slic, mark_boundaries
from skimage.measure import label,regionprops, perimeter
from skimage.morphology import ball, disk, dilation, binary_erosion, remove_small_objects, erosion, closing, reconstruction, binary_closing, watershed
from skimage.util import img_as_float
from skimage.filters import roberts, sobel
from skimage.feature import blob_dog, blob_log, blob_doh
import scipy.ndimage as ndi
from PIL import Image
import pickle,logging

logging.basicConfig(filename='log_file.txt', level=logging.DEBUG, format='%(asctime)s - %(levelname)s - %(message)s')
# In[413]:

def getSolution(id):
    csvpath = "./KaggleData/stage1/stage1_solution.csv"
    with open(csvpath) as f:
        reader = csv.reader(f)
        i = 0
        for row in reader:
            if i == id:
                return row[0], row[1]
            i += 1


# In[414]:

def get_slice_location(dcm):
    return float(dcm[0x0020, 0x1041].value)

# Returns a list of images for that patient_id, in ascending order of Slice Location
def load_patient(patient_images):
    imgs = []
    for image in patient_images:
        ds = dicom.read_file(image)
        imgs.append(ds)
    imgs.sort(key = lambda x: int(x.InstanceNumber))
    full_img = np.stack([s.pixel_array for s in imgs])
    return full_img    


# In[415]:

def save_images(patient_images):
    n_images = len(patient_images)
    for id in range(0, n_images-1):
        image = patient_images[id]
        image2 = image.copy()
        image_seg = segment_lungs(image2)
        image_seg[image_seg <  604] = 0
        image_seg2 = image_seg.copy()
        image_nob,image_nob2, n_keypoints = blob_segments_simple(image_seg2, dcm2rgb(image.copy()))
        
        
        image_nob = cv2.cvtColor(image_nob, cv2.COLOR_BGR2RGB)
        print("File = {0}/{1} : Number of blobs = {2}".format(id, n_images, n_keypoints))
        if(n_keypoints > 0):
            file_name = output_folder+patient+str(id)+".jpeg"
            print("Saved:", file_name)
            cv2.imwrite(file_name, image_nob)
#             plt.figure(figsize = (512,512))
#             plt.imshow(image_nob)
#             plt.savefig(file_name)
#             plt.close('all')


# In[416]:

def dcm2rgb(image):
    image2 = image.copy()
    image2[image2 == -2000] = 0
    for i in range(image2.shape[0]):
        for j in range(image2.shape[1]):
            image2[i][j] = np.interp(image2[i][j], [0,2250], [0,225])
    image2 = np.uint8(image2)
    image2 = cv2.cvtColor(image2, cv2.COLOR_GRAY2BGR)
    return image2


# In[417]:

def tr_mask(img):
    a = np.array([[263,312],[242,292],[212,241],[236,176],[276,220],[288,256],[263,312]])
    im1 = np.ones((img.shape[0], img.shape[1]))
    im1 = cv2.fillPoly(im1, [a], 0)
    hv = im1 == 0
    img[hv] = 0
    return img


# In[418]:

def segment_lungs(im, plot=False):
    
    '''
    This funtion segments the lungs from the given 2D slice.
    '''
    
    '''
    Step 1: Convert into a binary image. 
    '''
    binary = im < 604
 
    '''
    Step 2: Remove the blobs connected to the border of the image.
    '''
    cleared = clear_border(binary)
    
    '''
    Step 3: Label the image.
    '''
    label_image = label(cleared)
 
    '''
    Step 4: Keep the labels with 2 largest areas.
    '''
    areas = [r.area for r in regionprops(label_image)]
    areas.sort()
    if len(areas) > 2:
        for region in regionprops(label_image):
            if region.area < areas[-2]:
                for coordinates in region.coords:                
                       label_image[coordinates[0], coordinates[1]] = 0
    binary = label_image > 0
   
    '''
    Step 5: Erosion operation with a disk of radius 2. This operation is 
    seperate the lung nodules attached to the blood vessels.
    '''
    selem = disk(1)
    binary = binary_erosion(binary, selem)

    '''
    Step 6: Closure operation with a disk of radius 10. This operation is 
    to keep nodules attached to the lung wall.
    '''
    selem = disk(10)
    binary = binary_closing(binary, selem)
 
    '''
    Step 7: Fill in the small holes inside the binary mask of lungs.
    '''
    edges = roberts(binary)
    binary = ndi.binary_fill_holes(edges)

    '''
    Step 8: Superimpose the binary mask on the input image.
    '''
    get_high_vals = binary == 0
    im[get_high_vals] = 0

        
    return im


# In[419]:

def get_region_props(image, label_image):
    labeled_image = label(label_image)
    regions = regionprops(labeled_image, image, cache=True) 
    for rp in regions:
        fv = [rp.area, rp.centroid[0], rp.centroid[1], rp.convex_area, rp.eccentricity, rp.equivalent_diameter, rp.euler_number, rp.extent, rp.mean_intensity, rp.max_intensity, rp.min_intensity, rp.solidity, rp.weighted_centroid[0], rp.weighted_centroid[1]]
        print(fv)


# In[420]:

def blob_segments_simple(image, scan_image):
    image = np.array(image, np.uint8)
    dummy_image = np.zeros((image.shape[0], image.shape[1]))
    params = cv2.SimpleBlobDetector_Params()
    # Change thresholds
    params.minThreshold = 1;
    params.maxThreshold = 605;
    
    # Filter by Area.
    params.filterByColor = True
    params.blobColor = 255
#     params.maxArea =500
    
    # Filter by Area.
    params.filterByArea = True
    params.minArea = 30
    params.maxArea =150

    # Filter by Circularity
    params.filterByCircularity = True
    params.minCircularity = 0.1

#     Filter by Convexity
    params.filterByConvexity = True
    params.minConvexity = 0.001

    # Filter by Inertia
    params.filterByInertia = True
    params.minInertiaRatio = 0.2
    
    detector = cv2.SimpleBlobDetector_create(params)
    keypoints = detector.detect(image)
    n_keypoints = len(keypoints)
    for kpt in keypoints:
        dummy_image = cv2.circle(dummy_image, (int(kpt.pt[0]), int(kpt.pt[1])), int(kpt.size//2), color = (255,255,255), thickness = -1)
#     im_with_keypoints = cv2.drawKeypoints(scan_image, keypoints, np.array([]), (255,0,0), cv2.DRAW_MATCHES_FLAGS_DRAW_RICH_KEYPOINTS)
#     im2_with_keypoints = cv2.drawKeypoints(image, keypoints, np.array([]), (255,255,255), cv2.DRAW_MATCHES_FLAGS_DRAW_RICH_KEYPOINTS)
    points = dummy_image == 0
    image[points] = 0
    return image, dummy_image


# In[421]:

def dynamic_viz_temp(id):
    plt.close('all')
#     id = int(input_text.value)
    id = id + len(patient_images)//4
    image = patient_images[id]
    print(image.shape)
    image2 = image.copy()
    image2 = tr_mask(image2)
    image_seg = segment_lungs(image2)
    image_seg[image_seg <  604] = 0
    
    image_seg2 = image_seg.copy()
    
    image_nob, label_image = blob_segments_simple(image_seg2, dcm2rgb(image.copy()))
    get_region_props(image_nob, label_image)
    
#     fig, ax = plt.subplots(2,2, figsize = [20,20])
#     ax[0,0].imshow(image, cmap='gray')
#     ax[0,1].imshow(image_seg, cmap=plt.cm.bone)
#     ax[1,0].imshow(image_seg2, cmap='viridis')
#     ax[1,1].imshow(image_nob, cmap=plt.cm.bone)
#     plt.show()


# In[ ]:

def compute_feature_vector(patient_images):
    fv = []
    n_slices = patient_images.shape[0]
    total_area = 1
    avg_area = 1
    max_area = 1
    avg_eccentricity = 0
    avg_equivalent_diameter = 0
    std_equivalent_diameter = 0
    total_extent = 0 
    weightedX = 0.
    weightedY = 0.
    num_nodes = 1.
    num_nodes_per_slice = 1.
    
    areas = []
    eqi_diams = []
    image_id = 1
    
    for image in patient_images:
       	logging.debug("{2}Image {0}/{1}".format(image_id, n_slices, patient))
        image_id += 1
        image2 = image.copy()
        image2 = tr_mask(image2)
        image_seg = segment_lungs(image2)
        image_seg[image_seg <  604] = 0
        image_seg2 = image_seg.copy()
        image_nob, label_image = blob_segments_simple(image_seg2, dcm2rgb(image.copy()))
        labeled_image = label(label_image)
        regions = regionprops(labeled_image, image_nob, cache=True) 
        for rp in regions:
            total_area += rp.area
            areas.append(rp.area)
            avg_eccentricity += rp.eccentricity
            avg_equivalent_diameter += rp.equivalent_diameter
            eqi_diams.append(rp.equivalent_diameter)
            total_extent += rp.extent
            weightedX += rp.centroid[0]*rp.area
            weightedY += rp.centroid[1]*rp.area
            num_nodes += 1
    weightedX = weightedX / total_area
    weightedY = weightedY / total_area
    avg_area = total_area / num_nodes
    avg_eccentricity = avg_eccentricity / num_nodes
    avg_equivalent_diameter = avg_equivalent_diameter / num_nodes
    std_equivalent_diameter = np.std(eqi_diams)
    max_area = max(areas)
    num_nodes_per_slice = num_nodes * 1. / n_slices
    return np.array([avg_area, max_area, avg_eccentricity, avg_equivalent_diameter, std_equivalent_diameter,                      total_extent, weightedX, weightedY, num_nodes, num_nodes_per_slice])


# In[ ]:

image_folder = "./KaggleData/stage1/data/stage1/"
output_folder = "./KaggleData/stage1/data/output/"
patients = os.listdir(image_folder)
randN = random.randint(0, 199)
for i in range(166,199):
    patient, cancer_label = getSolution(i)
    logging.debug("**************************Computing features for patient: {0}. Remaining: {1}/{2} **************************".format(patient, i, 198))
    patient_images = os.listdir(image_folder+patient+"/")
    patient_images = [image_folder+patient+"/"+image for image in patient_images]
    patient_images = load_patient(patient_images)
    try:
        dataX = compute_feature_vector(patient_images)
        dataY = int(cancer_label)
        logging.debug("saving feature vectors to file!")
        np.save("./tr_data/"+patient+"dataX.npy", dataX)
        np.save("./tr_data/"+patient+"dataY.npy", dataY)
    except:
        print("No segments found, skipping patient!!!")
        continue

